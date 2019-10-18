/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 Andres Almiray.
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
package org.kordamp.maven

import groovy.transform.CompileStatic

import java.nio.file.Paths

import static StringUtils.isBlank
import static StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class PropertyUtils {
    static String stringProperty(String envKey, String propertyKey, String alternateValue) {
        String value = System.getenv(envKey)
        if (isBlank(value)) value = System.getProperty(propertyKey)
        return isNotBlank(value) ? value : alternateValue
    }

    static boolean booleanProperty(String envKey, String propertyKey, boolean alternateValue) {
        String value = System.getenv(envKey)
        if (isBlank(value)) value = System.getProperty(propertyKey)
        if (isNotBlank(value)) {
            return Boolean.parseBoolean(value)
        }
        return alternateValue
    }

    static int integerProperty(String envKey, String propertyKey, int alternateValue) {
        String value = System.getenv(envKey)
        if (isBlank(value)) value = System.getProperty(propertyKey)
        if (isNotBlank(value)) {
            return Integer.parseInt(value)
        }
        return alternateValue
    }

    static File fileProperty(String envKey, String propertyKey, File alternateValue) {
        String value = System.getenv(envKey)
        if (isBlank(value)) value = System.getProperty(propertyKey)
        if (isNotBlank(value)) {
            return Paths.get(value).toFile()
        }
        return alternateValue
    }
}
