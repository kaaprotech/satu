/*
 * Copyright 2014 Kaaprotech Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaaprotech.satu.parser;

import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Sets;

@SuppressWarnings("serial")
public final class CompilationUnitValidator {

    private final ImmutableSet<String> primitiveTypes_;

    private final ImmutableSet<String> importedTypes_;

    private final ImmutableSet<String> reservedWords_;

    public CompilationUnitValidator() {
        final MutableSet<String> pts = Sets.mutable.of();
        for (PrimitiveType pt : PrimitiveType.values()) {
            pts.add(String.valueOf(pt).toLowerCase());
        }
        primitiveTypes_ = pts.toImmutable();

        final MutableSet<String> its = Sets.mutable.of();
        for (ImportedType it : ImportedType.values()) {
            its.add(String.valueOf(it).toLowerCase());
        }
        importedTypes_ = its.toImmutable();

        final MutableSet<String> reservedWords = Sets.mutable.of();
        reservedWords.addAll(pts);
        reservedWords.addAll(its);
        for (ReservedWord rw : ReservedWord.values()) {
            reservedWords.add(String.valueOf(rw).toLowerCase());
        }
        reservedWords_ = reservedWords.toImmutable();
    }

    public void validate(final CompilationUnit cu) {
        validateDeclaredTypeNames(cu);
        validateDeclaredTypes(cu, cu.getDeclaredTypesMap().toImmutable());
    }

    private void validateDeclaredTypeNames(final CompilationUnit cu) {
        final MutableSet<String> names = Sets.mutable.of();
        cu.getDeclaredTypes().forEach(new Procedure<DeclaredType>() {
            @Override
            public void value(final DeclaredType dt) {
                if (names.contains(dt.getName().toLowerCase())) {
                    throw new RuntimeException("Duplicate declared type name " + dt.getName());
                }
                if (reservedWords_.contains(dt.getName().toLowerCase())) {
                    throw new RuntimeException("Reserved word used for declared type name " + dt.getName());
                }
                names.add(dt.getName().toLowerCase());
            }
        });
    }

    private void validateDeclaredTypes(final CompilationUnit cu, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        cu.getDeclaredTypes().forEach(new Procedure<DeclaredType>() {
            @Override
            public void value(final DeclaredType dt) {
                validateDeclaredType(dt, declaredTypeMap);
            }
        });
    }

    private void validateDeclaredType(final DeclaredType dt, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        switch (dt.getDeclaredTypeCategory()) {
        case Model:
            validateClass(dt, declaredTypeMap);
            break;

        case Key:
            validateKeyClass(dt, declaredTypeMap);
            break;

        case Enum:
            validateEnum(dt);
            break;

        default:
            throw new RuntimeException("Unexpected enum " + dt.getDeclaredTypeCategory());
        }
    }

    private void validateClass(final DeclaredType dt, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        validateClassKeyField(dt);
        validateClassMapFields(dt, declaredTypeMap);
        validateSetFields(dt, declaredTypeMap);
        validateDeclaredTypeFields(dt, declaredTypeMap);
    }

    private void validateClassMapFields(final DeclaredType declaredType, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        declaredType.getFields().select(new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                return field.getFieldTypeCategory() == FieldTypeCategory.Map;
            }
        }).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field mapField) {
                final String keyTypeArg = mapField.getTypeArguments().getFirst();
                // Primitive and Imported types are valid keys
                if (!primitiveTypes_.contains(keyTypeArg.toLowerCase()) && !importedTypes_.contains(keyTypeArg.toLowerCase())) {
                    final DeclaredType dt = declaredTypeMap.get(keyTypeArg);
                    if (dt == null) {
                        throw new RuntimeException("Map key type " + keyTypeArg + " not defined for field " + declaredType.getName() + "." + mapField.getName());
                    }
                    switch (dt.getDeclaredTypeCategory()) {
                    case Enum:
                    case Key:
                        break;
                    default:
                        throw new RuntimeException("Map key type " + keyTypeArg + " invalid for field " + declaredType.getName() + "." + mapField.getName());
                    }
                }
                final String valueTypeArg = mapField.getTypeArguments().get(1);
                if (!primitiveTypes_.contains(valueTypeArg.toLowerCase()) && !importedTypes_.contains(valueTypeArg.toLowerCase())) {
                    final DeclaredType dt = declaredTypeMap.get(valueTypeArg);
                    if (dt == null) {
                        throw new RuntimeException("Map value type " + valueTypeArg + " not defined for field " + declaredType.getName() + "." + mapField.getName());
                    }
                }
            }
        });
    }

    private void validateSetFields(final DeclaredType declaredType, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        declaredType.getFields().select(new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                return field.getFieldTypeCategory() == FieldTypeCategory.Set;
            }
        }).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field setField) {
                final String elementTypeArg = setField.getTypeArguments().getFirst();
                if (!primitiveTypes_.contains(elementTypeArg.toLowerCase()) && !importedTypes_.contains(elementTypeArg.toLowerCase())) {
                    final DeclaredType dt = declaredTypeMap.get(elementTypeArg);
                    if (dt == null) {
                        throw new RuntimeException("Set element type " + elementTypeArg + " not defined for field " + declaredType.getName() + "." + setField.getName());
                    }
                    switch (dt.getDeclaredTypeCategory()) {
                    case Enum:
                    case Key:
                        break;
                    default:
                        throw new RuntimeException(
                                "Set element type " + elementTypeArg + " invalid for field " + declaredType.getName() + "." + setField.getName() + ", only immutable types are valid");
                    }
                }
            }
        });
    }

    private void validateDeclaredTypeFields(final DeclaredType declaredType, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        declaredType.getFields().select(new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                return field.getFieldTypeCategory() == FieldTypeCategory.DeclaredType;
            }
        }).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                if (!declaredTypeMap.containsKey(field.getTypeName())) {
                    throw new RuntimeException(field.getTypeName() + " type not defined for field " + declaredType.getName() + "." + field.getName());
                }
            }
        });
    }

    private void validateClassKeyField(final DeclaredType dt) {
        final ImmutableList<Field> keyFields = dt.getFields().select(new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                return field.getModifier() == FieldModifier.key;
            }
        }).toImmutable();

        if (keyFields.size() == 0) {
            throw new RuntimeException("Missing key field for " + dt.getName() + " class");
        }

        if (keyFields.size() > 1) {
            final StringBuilder builder = new StringBuilder();
            keyFields.forEach(new Procedure<Field>() {
                @Override
                public void value(final Field f) {
                    if (builder.length() == 0) {
                        builder.append(f.getName());
                    }
                    else {
                        builder.append(", " + f.getName());
                    }
                }
            });
            throw new RuntimeException("Multiple key fields " + builder + " for " + dt.getName() + " class");
        }

        final Field field = keyFields.getFirst();
        switch (field.getFieldTypeCategory()) {
        case Map:
            // TODO: Both key and value must be immutable
            break;

        default:
            break;
        }
    }

    private void validateKeyClass(final DeclaredType dt, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        validateKeyClassFieldModifiers(dt);
        validateKeyClassMapFields(dt, declaredTypeMap);
        validateSetFields(dt, declaredTypeMap);
        validateDeclaredTypeFields(dt, declaredTypeMap);
    }

    private void validateKeyClassFieldModifiers(final DeclaredType declaredType) {
        declaredType.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                if (field.getModifier() != FieldModifier.val) {
                    throw new RuntimeException("Field modifier " + field.getModifier() + " invalid for key class " + declaredType.getName() + "." + field.getName());
                }
            }
        });
    }

    private void validateKeyClassMapFields(final DeclaredType declaredType, final ImmutableMap<String, DeclaredType> declaredTypeMap) {
        declaredType.getFields().select(new Predicate<Field>() {
            @Override
            public boolean accept(final Field field) {
                return field.getFieldTypeCategory() == FieldTypeCategory.Map;
            }
        }).forEach(new Procedure<Field>() {
            @Override
            public void value(final Field mapField) {
                mapField.getTypeArguments().forEach(new Procedure<String>() {
                    @Override
                    public void value(final String typeArg) {
                        if (!primitiveTypes_.contains(typeArg.toLowerCase())) {
                            final DeclaredType dt = declaredTypeMap.get(typeArg);
                            if (dt == null) {
                                throw new RuntimeException("Map type argument " + typeArg + " not defined for field " + declaredType.getName() + "." + mapField.getName());
                            }
                            switch (dt.getDeclaredTypeCategory()) {
                            case Enum:
                            case Key:
                                break;
                            default:
                                throw new RuntimeException("Map type argument " + typeArg + " invalid for key class field " + declaredType.getName() + "." + mapField.getName());
                            }
                        }
                    }
                });
            }
        });
    }

    private void validateEnum(final DeclaredType declaredType) {
        final MutableSet<String> names = Sets.mutable.of();
        declaredType.getFields().forEach(new Procedure<Field>() {
            @Override
            public void value(final Field field) {
                if (!names.add(field.getName())) {
                    throw new RuntimeException("Duplicate enum field " + declaredType.getName() + "." + field.getName());
                }
            }
        });
    }
}
