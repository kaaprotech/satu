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

public enum PrimitiveType {
    String("String", "String"),
    Boolean("boolean", "Boolean"),
    Char("char", "Character"),
    Byte("byte", "Byte"),
    Short("short", "Short"),
    Int("int", "Integer"),
    Long("long", "Long"),
    Float("float", "Float"),
    Double("double", "Double");

    private final String string_;

    private final String wrapperClass_;

    private PrimitiveType(String string, String wrapperClass) {
        string_ = string;
        wrapperClass_ = wrapperClass;
    }

    public String getString() {
        return string_;
    }

    public String getWrapperClass() {
        return wrapperClass_;
    }
}
