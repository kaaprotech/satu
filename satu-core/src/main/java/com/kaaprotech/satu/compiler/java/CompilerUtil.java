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

package com.kaaprotech.satu.compiler.java;

import com.gs.collections.api.block.predicate.Predicate;
import com.kaaprotech.satu.parser.DeclaredType;
import com.kaaprotech.satu.parser.Field;
import com.kaaprotech.satu.parser.FieldModifier;
import com.kaaprotech.satu.parser.ImportedType;
import com.kaaprotech.satu.parser.PrimitiveType;

@SuppressWarnings("serial")
public final class CompilerUtil {

    public static final String BF = "BuilderField__";
    
    public static final String DBF = "DeltaBuilderField__";
    
    public static final String REF = "ref__";

    public static final String DT = "deltaType__";

    public static final String LDT = "lastDeltaType__";

    public static final String MBS = "builderFieldBitSet__";

    public static final String DBS = "deltaBuilderFieldBitSet__";

    public static final String LS = System.getProperty("line.separator");

    public static final String TAB = "    ";    

	public static String getKeyFieldType(final DeclaredType dt) {
		switch (dt.getDeclaredTypeCategory()) {
		case Key:
			return dt.getName();
		case Model:
			return getFieldType(getKeyField(dt));
		default:
			throw new RuntimeException("Unexpected enum " + dt.getDeclaredTypeCategory());
		}
	}

	public static Field getKeyField(final DeclaredType dt) {
		return dt.getFields().detect(new Predicate<Field>() {
			@Override
			public boolean accept(final Field field) {
				return field.getModifier() == FieldModifier.key;
			}
		});
	}

	public static String getFieldType(final Field field) {
		switch (field.getFieldTypeCategory()) {
		case Primitive:
			return PrimitiveType.valueOf(field.getTypeName()).getWrapperClass();
		case ImportedType:
			return ImportedType.valueOf(field.getTypeName()).getWrapperClass();
		case DeclaredType:
			return field.getTypeName();
		case Set:
			return "ImmutableSet<" + field.getTypeArgs().get(0) + ">";
		case Map:
			return "ImmutableMap<" + field.getTypeArgs().get(0) + ", " + field.getTypeArgs().get(1) + ">";
		default:
			throw new RuntimeException("Unexpected enum " + field.getFieldTypeCategory());
		}
	}
}
