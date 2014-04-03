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

grammar Satu;

// Starting point for parsing a *.satu definition file

compilationUnit
    :   packageDeclaration
        importDeclaration*
        typeDeclaration*
        EOF
    ;

packageDeclaration
    :   'package' qualifiedName
    ;

importDeclaration
    :   'import' qualifiedName ('.' '*')? ';'
    ;

typeDeclaration
    :   (   modelClassDeclaration
        |   keyClassDeclaration
        |   enumDeclaration
        )
    |   ';'
    ;

modelClassDeclaration
    :   'class' Identifier
        classBody
    ;

keyClassDeclaration
    :   'key' 'class' Identifier
        classBody
    ;

enumDeclaration
    :   'enum'  Identifier
        enumBody
    ;

enumBody
    :   '{' enumConstants? '}'
    ;

enumConstants
    :   Identifier (',' Identifier)*
    ;

enumValue
    :   Identifier
    ;

classBody
    :   '{' fieldDeclaration* '}'
    ;

type
    :   setType
    |   mapType
    |   classType ('[' ']')*
    |   primitiveType
    |   importedType
    ;

setType
    :   'Set' setTypeArgument
    ;

setTypeArgument
    :   '<' type '>'
    ;

mapType
    :   'Map' mapTypeArguments
    ;

mapTypeArguments
    :   '<' type ',' type '>'
    ;

classType
    :   Identifier
    ;

primitiveType
    :   'String'
    |   'Boolean'
    |   'Char'
    |   'Byte'
    |   'Short'
    |   'Int'
    |   'Long'
    |   'Float'
    |   'Double'
    ;

importedType
    :   'DateTime'
    ;

fieldDeclaration
    :   annotation* fieldModifier fieldDeclaratorId ':' type ('=' fieldInitializer)? ';'
    ;

fieldModifier
    :   'key'
    |   'val'
    |   'var'
    ;

fieldDeclaratorId
    :   Identifier ('[' ']')*
    ;

fieldInitializer
    :   literal
    |   enumValue
    ;

qualifiedName
    :   Identifier ('.' Identifier)*
    ;

literal
    :   DecimalLiteral
    |   FloatingPointLiteral
    |   CharacterLiteral
    |   StringLiteral
    |   booleanLiteral
    |   'null'
    ;

annotation
    :   '@'annotationName ( '(' StringLiteral ')' )?
    ;

annotationName
    :   qualifiedName
    ;

booleanLiteral
    :   'true'
    |   'false'
    ;

DecimalLiteral
    : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    :   ('l'|'L')
    ;

FloatingPointLiteral
    :   ('0'..'9')+ '.' ('0'..'9')* Exponent? FloatTypeSuffix?
    |   '.' ('0'..'9')+ Exponent? FloatTypeSuffix?
    |   ('0'..'9')+ Exponent FloatTypeSuffix?
    |   ('0'..'9')+ FloatTypeSuffix
    ;

fragment
Exponent
    :   ('e'|'E') ('+'|'-')? ('0'..'9')+
    ;

fragment
FloatTypeSuffix
    :   ('f'|'F'|'d'|'D')
    ;

CharacterLiteral
    :   '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
    ;

StringLiteral
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

Identifier
    :   Letter (Letter|JavaIDDigit)*
    ;

fragment
Letter
    :   '\u0024'
    |   '\u0041'..'\u005a'
    |   '\u005f'
    |   '\u0061'..'\u007a'
    |   '\u00c0'..'\u00d6'
    |   '\u00d8'..'\u00f6'
    |   '\u00f8'..'\u00ff'
    |   '\u0100'..'\u1fff'
    |   '\u3040'..'\u318f'
    |   '\u3300'..'\u337f'
    |   '\u3400'..'\u3d2d'
    |   '\u4e00'..'\u9fff'
    |   '\uf900'..'\ufaff'
    ;

fragment
JavaIDDigit
    :   '\u0030'..'\u0039'
    |   '\u0660'..'\u0669'
    |   '\u06f0'..'\u06f9'
    |   '\u0966'..'\u096f'
    |   '\u09e6'..'\u09ef'
    |   '\u0a66'..'\u0a6f'
    |   '\u0ae6'..'\u0aef'
    |   '\u0b66'..'\u0b6f'
    |   '\u0be7'..'\u0bef'
    |   '\u0c66'..'\u0c6f'
    |   '\u0ce6'..'\u0cef'
    |   '\u0d66'..'\u0d6f'
    |   '\u0e50'..'\u0e59'
    |   '\u0ed0'..'\u0ed9'
    |   '\u1040'..'\u1049'
    ;

COMMENT
    :   '/*' .*? '*/' -> skip // match anything between /* and */
    ;

WS
    :   (' '|'\r'|'\t'|'\u000C'|'\n')+ -> skip // skip spaces, tabs, newlines
    ;

LINE_COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' -> skip
    ;
