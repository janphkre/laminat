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

package org.apache.http.entity;

import org.apache.http.Consts;
import org.apache.http.util.Args;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Content type information consisting of a MIME type and an optional charset.
 * <p>
 * This class makes no attempts to verify validity of the MIME type.
 * The input parameters of the {@link #create(String, String)} method, however, may not
 * contain characters {@code <">, <;>, <,>} reserved by the HTTP specification.
 *
 * @since 4.2
 */
public final class ContentType implements Serializable {

    private static final long serialVersionUID = -7768694718232371896L;

    // constants

    public static final String CONTENT_TYPE = "Content-Type";

    public static final ContentType APPLICATION_ATOM_XML = create(
            "application/atom+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_FORM_URLENCODED = create(
            "application/x-www-form-urlencoded", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create(
            "application/json", Consts.UTF_8);
    public static final ContentType APPLICATION_JSON_RPC = create(
            "application/json-rpc", Consts.UTF_8);
    public static final ContentType APPLICATION_JSONREQUEST = create(
            "application/jsonrequest", Consts.UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = create(
            "application/octet-stream", (Charset) null);
    public static final ContentType APPLICATION_SVG_XML = create(
            "application/svg+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_XHTML_XML = create(
            "application/xhtml+xml", Consts.ISO_8859_1);
    public static final ContentType APPLICATION_XML = create(
            "application/xml", Consts.ISO_8859_1);
    public static final ContentType IMAGE_BMP = create(
            "image/bmp");
    public static final ContentType IMAGE_GIF= create(
            "image/gif");
    public static final ContentType IMAGE_JPEG = create(
            "image/jpeg");
    public static final ContentType IMAGE_PNG = create(
            "image/png");
    public static final ContentType IMAGE_SVG= create(
            "image/svg+xml");
    public static final ContentType IMAGE_TIFF = create(
            "image/tiff");
    public static final ContentType IMAGE_WEBP = create(
            "image/webp");
    public static final ContentType MULTIPART_FORM_DATA = create(
            "multipart/form-data", Consts.ISO_8859_1);
    public static final ContentType TEXT_HTML = create(
            "text/html", Consts.ISO_8859_1);
    public static final ContentType TEXT_PLAIN = create(
            "text/plain", Consts.ISO_8859_1);
    public static final ContentType TEXT_XML = create(
            "text/xml", Consts.ISO_8859_1);
    public static final ContentType WILDCARD = create(
            "*/*", null);

    // defaults
    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    private final String mimeType;
    private final Charset charset;

    ContentType(
            final String mimeType,
            final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a {@code Content-Type} header.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.mimeType);
        if (this.charset != null) {
            builder.append("; charset=");
            builder.append(this.charset.name());
        }
        return builder.toString();
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';') {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new instance of {@link ContentType}.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @param charset charset.
     * @return content type
     */
    private static ContentType create(final String mimeType, final Charset charset) {
        final String normalizedMimeType = Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
        Args.check(valid(normalizedMimeType), "MIME type may not contain reserved characters");
        return new ContentType(normalizedMimeType, charset);
    }

    /**
     * Creates a new instance of {@link ContentType} without a charset.
     *
     * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain
     *        characters {@code <">, <;>, <,>} reserved by the HTTP specification.
     * @return content type
     */
    private static ContentType create(final String mimeType) {
        return create(mimeType, null);
    }
}
