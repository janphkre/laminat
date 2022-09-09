package au.com.dius.pact.model

import au.com.dius.pact.external.util.toUtf8String
import au.com.dius.pact.matchers.MatchingConfig
import au.com.dius.pact.model.matchingrules.MatchingRules
import org.apache.http.Consts
import java.util.Locale
import kotlin.math.min
import org.apache.http.entity.ContentType

abstract class HttpPart {

    abstract val body: OptionalBody
    abstract var headers: Map<String, String>
    abstract val matchingRules: MatchingRules

    fun charset(): String? {// TODO: use charset in other parts of the code!
        val contentTypeKey = headers.keys.find { ContentType.CONTENT_TYPE.equals(it, true) }
        if (contentTypeKey != null) {
            val entryList = headers[contentTypeKey]!!.split(';').drop(1)
            for (entry in entryList) {
                val entryKeyValue= entry.split("=")
                if (entryKeyValue.first().contains("charset")) {
                    return entryKeyValue.last()
                }
            }
        }
        return null
    }

    fun mimeType(): String {
        val contentTypeKey = headers.keys.find { ContentType.CONTENT_TYPE.equals(it, true) }
        return if (contentTypeKey != null) {
            headers[contentTypeKey]!!.split(';').first()
        } else {
            detectContentType()
        }
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