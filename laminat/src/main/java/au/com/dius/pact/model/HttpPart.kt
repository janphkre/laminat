package au.com.dius.pact.model

import au.com.dius.pact.matchers.MatchingConfig
import au.com.dius.pact.model.matchingrules.MatchingRules
import java.util.Locale
import kotlin.math.min
import org.apache.http.entity.ContentType

abstract class HttpPart {

    abstract val body: OptionalBody
    abstract var headers: Map<String, String>
    abstract val matchingRules: MatchingRules

    fun mimeType(): String {
        val contentTypeKey = headers.keys.find { ContentType.CONTENT_TYPE.equals(it, true) }
        return if (contentTypeKey != null) {
            headers[contentTypeKey]!!.split(';').first()
        } else {
            detectContentType()
        }
    }

    private fun detectContentType(): String {
        val body = body
        return if (body is OptionalBody.StringBody) {
            val bodyString = body.unwrap()
            val s = bodyString.substring(0, min(bodyString.length, 32)).filter { it != '\n' }
            if (XMLREGEXP.matches(s)) {
                ContentType.APPLICATION_XML.mimeType
            } else if (HTMLREGEXP.matches(s.uppercase(Locale.ROOT))) {
                ContentType.TEXT_HTML.mimeType
            } else if (JSONREGEXP.matches(s)) {
                ContentType.APPLICATION_JSON.mimeType
            } else if (XMLREGEXP2.matches(s)) {
                ContentType.APPLICATION_XML.mimeType
            } else {
                ContentType.TEXT_PLAIN.mimeType
            }
        } else if (body is OptionalBody.BinaryBody) {
            ContentType.DEFAULT_BINARY.mimeType
        } else {
            ContentType.TEXT_PLAIN.mimeType
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