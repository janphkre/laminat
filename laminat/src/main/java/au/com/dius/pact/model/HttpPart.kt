package au.com.dius.pact.model

import au.com.dius.pact.external.util.toUtf8String
import au.com.dius.pact.matchers.MatchingConfig
import au.com.dius.pact.model.matchingrules.MatchingRules
import java.nio.charset.Charset
import java.util.Locale
import kotlin.math.min
import org.apache.http.entity.ContentType

abstract class HttpPart {

    abstract val body: OptionalBody
    abstract val headers: Map<String, String>
    abstract val matchingRules: MatchingRules

    fun charset(): Charset {
        val contentTypeKey = contentTypeHeaderKey()
        if (contentTypeKey != null) {
            val contentType = headers[contentTypeKey]!!

            val explicitCharset = try {
                Charset.forName(explicitCharsetStringFromHeader(contentType))
            } catch (e: Exception) {
                null
            }

            if (explicitCharset != null) {
                return explicitCharset
            }
            return contentType.mapToCharset()
        }
        return detectContentType().mapToCharset()
    }

    private fun String.mapToCharset(): Charset {
        return when {
            startsWith(ContentType.APPLICATION_ATOM_XML.mimeType, ignoreCase = true) -> ContentType.APPLICATION_ATOM_XML.charset
            startsWith(ContentType.APPLICATION_FORM_URLENCODED.mimeType, ignoreCase = true) -> ContentType.APPLICATION_FORM_URLENCODED.charset
            startsWith(ContentType.APPLICATION_JSON.mimeType, ignoreCase = true) -> ContentType.APPLICATION_JSON.charset
            startsWith(ContentType.APPLICATION_JSON_RPC.mimeType, ignoreCase = true) -> ContentType.APPLICATION_JSON_RPC.charset
            startsWith(ContentType.APPLICATION_JSONREQUEST.mimeType, ignoreCase = true) -> ContentType.APPLICATION_JSONREQUEST.charset
            startsWith(ContentType.APPLICATION_SVG_XML.mimeType, ignoreCase = true) -> ContentType.APPLICATION_SVG_XML.charset
            startsWith(ContentType.APPLICATION_XHTML_XML.mimeType, ignoreCase = true) -> ContentType.APPLICATION_XHTML_XML.charset
            startsWith(ContentType.APPLICATION_XML.mimeType, ignoreCase = true) -> ContentType.APPLICATION_XML.charset
            startsWith(ContentType.MULTIPART_FORM_DATA.mimeType, ignoreCase = true) -> ContentType.MULTIPART_FORM_DATA.charset
            startsWith(ContentType.TEXT_HTML.mimeType, ignoreCase = true) -> ContentType.TEXT_HTML.charset
            startsWith(ContentType.TEXT_PLAIN.mimeType, ignoreCase = true) -> ContentType.TEXT_PLAIN.charset
            startsWith(ContentType.TEXT_XML.mimeType, ignoreCase = true) -> ContentType.TEXT_XML.charset
            else -> null
        } ?: Charsets.UTF_8
    }

    private fun explicitCharsetStringFromHeader(header: String): String? {
        val entryList = header.split(';').drop(1)
        for (entry in entryList) {
            val entryKeyValue = entry.split("=")
            if (entryKeyValue.first().contains("charset")) {
                return entryKeyValue.last()
            }
        }
        return null
    }

    fun mimeType(): String {
        val contentTypeKey = contentTypeHeaderKey()
        return if (contentTypeKey != null) {
            headers[contentTypeKey]!!.split(';').first()
        } else {
            detectContentType()
        }
    }

    private fun contentTypeHeaderKey(): String? {
        return headers.keys.find { ContentType.CONTENT_TYPE.equals(it, true) }
    }

    private fun detectContentType(): String {
        return when (val body = body) {
            is OptionalBody.StringBody -> {
                val bodyString = body.unwrap()
                detectContentTypeFromBody(bodyString) ?: ContentType.TEXT_PLAIN.mimeType
            }
            is OptionalBody.BinaryBody -> {
                val bodyString = body.unwrap().toUtf8String()
                detectContentTypeFromBody(bodyString) ?: ContentType.DEFAULT_BINARY.mimeType
            }
            else -> {
                ContentType.TEXT_PLAIN.mimeType
            }
        }
    }

    private fun detectContentTypeFromBody(body: String): String? {
        val s = body.substring(0, min(body.length, 32)).filter { it != '\n' }
        return if (XMLREGEXP.matches(s)) {
            ContentType.APPLICATION_XML.mimeType
        } else if (HTMLREGEXP.matches(s.uppercase(Locale.ROOT))) {
            ContentType.TEXT_HTML.mimeType
        } else if (JSONREGEXP.matches(s)) {
            ContentType.APPLICATION_JSON.mimeType
        } else if (XMLREGEXP2.matches(s)) {
            ContentType.APPLICATION_XML.mimeType
        } else {
            null
        }
    }

    fun jsonBody(): Boolean {
        return MatchingConfig.isJson(mimeType())
    }

    fun xmlBody(): Boolean {
        return MatchingConfig.isXml(mimeType())
    }

    companion object {
        val XMLREGEXP by lazy { Regex("^\\s*<\\?xml\\s*version.*") }
        val HTMLREGEXP by lazy { Regex("^\\s*(<!DOCTYPE)|(<HTML>).*") }
        val JSONREGEXP by lazy { Regex("^\\s*(true|false|null|[0-9]+|\"\\w*|\\{\\s*(\\}|\"\\w+)|\\[\\s*).*") }
        val XMLREGEXP2 by lazy { Regex("^\\s*<\\w+\\s*(:\\w+=[\"”][^\"”]+[\"”])?.*") }
    }
}