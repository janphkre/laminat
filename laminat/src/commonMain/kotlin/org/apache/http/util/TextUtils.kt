/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
/*
 * Edited by Jan Phillip Kretzschmar (github.com/janphkre)
 * - Removed unused methods
 * - Made some methods package internal
 */
package org.apache.http.util

/**
 * @since 4.3
 */
object TextUtils {
    /**
     * Returns true if the parameter is null or of zero length
     */
    fun isEmpty(s: CharSequence?): Boolean {
        return s?.isEmpty() ?: true
    }

    /**
     * Returns true if the parameter is null or contains only whitespace
     */
    fun isBlank(s: CharSequence?): Boolean {
        if (s == null) {
            return true
        }
        for (element in s) {
            if (!element.isWhitespace()) {
                return false
            }
        }
        return true
    }
}