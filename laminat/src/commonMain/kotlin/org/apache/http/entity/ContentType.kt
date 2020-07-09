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
 * - Removed Contract annotation
 * - Removed unused methods
 * - Made methods private, constants should be used.
 * - Replaced CharArrayBuffer with StringBuilder in toString()
 * - Added json-rpc and jsonrequest content types
 * - Added constant containing "Content-Type"
 */
package org.apache.http.entity

import org.apache.http.Consts
import org.apache.http.util.Args.check
import org.apache.http.util.Args.notBlank

/**
 * Content type information consisting of a MIME type and an optional charset.
 *
 *
 * This class makes no attempts to verify validity of the MIME type.
 * The input parameters of the [.create] method, however, may not
 * contain characters `<">, <;>, <,>` reserved by the HTTP specification.
 *
 * @since 4.2
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ContentType internal constructor(
    val mimeType: String,
    private val charset: Charset?
) {

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a `Content-Type` header.
     */
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(mimeType)
        if (charset != null) {
            builder.append("; charset=")
            builder.append(charset.name())
        }
        return builder.toString()
    }

    companion object {
        private const val serialVersionUID = -7768694718232371896L

        // constants
        const val CONTENT_TYPE = "Content-Type"
        val APPLICATION_ATOM_XML = create(
            "application/atom+xml", Consts.ISO_8859_1
        )
        val APPLICATION_FORM_URLENCODED = create(
            "application/x-www-form-urlencoded", Consts.ISO_8859_1
        )
        val APPLICATION_JSON = create(
            "application/json", Consts.UTF_8
        )
        val APPLICATION_JSON_RPC = create(
            "application/json-rpc", Consts.UTF_8
        )
        val APPLICATION_JSONREQUEST = create(
            "application/jsonrequest", Consts.UTF_8
        )
        val APPLICATION_OCTET_STREAM = create(
            "application/octet-stream", null as Charset?
        )
        val APPLICATION_SVG_XML = create(
            "application/svg+xml", Consts.ISO_8859_1
        )
        val APPLICATION_XHTML_XML = create(
            "application/xhtml+xml", Consts.ISO_8859_1
        )
        val APPLICATION_XML = create(
            "application/xml", Consts.ISO_8859_1
        )
        val IMAGE_BMP = create(
            "image/bmp"
        )
        val IMAGE_GIF = create(
            "image/gif"
        )
        val IMAGE_JPEG = create(
            "image/jpeg"
        )
        val IMAGE_PNG = create(
            "image/png"
        )
        val IMAGE_SVG = create(
            "image/svg+xml"
        )
        val IMAGE_TIFF = create(
            "image/tiff"
        )
        val IMAGE_WEBP = create(
            "image/webp"
        )
        val MULTIPART_FORM_DATA = create(
            "multipart/form-data", Consts.ISO_8859_1
        )
        val TEXT_HTML = create(
            "text/html", Consts.ISO_8859_1
        )
        val TEXT_PLAIN = create(
            "text/plain", Consts.ISO_8859_1
        )
        val TEXT_XML = create(
            "text/xml", Consts.ISO_8859_1
        )
        val WILDCARD = create(
            "*/*", null
        )

        // defaults
        val DEFAULT_TEXT = TEXT_PLAIN
        val DEFAULT_BINARY = APPLICATION_OCTET_STREAM

        private fun valid(s: String): Boolean {
            for (element in s) {
                if (element == '"' || element == ',' || element == ';') {
                    return false
                }
            }
            return true
        }

        /**
         * Creates a new instance of [ContentType].
         *
         * @param mimeType MIME type. It may not be `null` or empty. It may not contain
         * characters `<">, <;>, <,>` reserved by the HTTP specification.
         * @param charset charset.
         * @return content type
         */
        private fun create(mimeType: String, charset: Charset? = null): ContentType {
            val normalizedMimeType = notBlank(mimeType, "MIME type").toLowerCase()
            check(valid(normalizedMimeType), "MIME type may not contain reserved characters")
            return ContentType(normalizedMimeType, charset)
        }
    }
}