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

public enum ImportedType {

    DateTime("DateTime", "import org.joda.time.DateTime;");

    private final String wrapperClass_;

    private final String importStatement_;

    private ImportedType(String wrapperClass, String importStatement) {
        wrapperClass_ = wrapperClass;
        importStatement_ = importStatement;
    }

    public String getWrapperClass() {
        return wrapperClass_;
    }

    public String getImportStatement() {
        return importStatement_;
    }
}
