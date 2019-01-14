package au.com.dius.pact.model

import au.com.dius.pact.model.matchingrules.MatchingRules
import java.util.regex.Pattern

abstract class HttpPart {

    abstract val body: OptionalBody
    abstract var headers: Map<String, String>
    abstract val matchingRules: MatchingRules

    fun mimeType(): String {
        val contentTypeKey = headers.keys.find { CONTENT_TYPE.equals(it, true) }
        return if (contentTypeKey != null) {
            headers[contentTypeKey]!!.split(';').first()
        } else {
            detectContentType()
        }
    }

    private fun detectContentType(): String {
        return if (body.isPresent()) {
            val s = body.value!!.substring(0,Math.min(body.value!!.length, 32)).filter { it != '\n' }
            if (XMLREGEXP.matches(s)) {
                "application/xml"
            } else if (HTMLREGEXP.matches(s.toUpperCase())) {
                "text/html"
            } else if (JSONREGEXP.matches(s)) {
                "application/json"
            } else if (XMLREGEXP2.matches(s)) {
                "application/xml"
            } else {
                "text/plain"
            }
        } else {
            "text/plain"
        }
    }

    fun jsonBody(): Boolean {
        return Pattern.matches("application/.*json", mimeType())
    }

    fun xmlBody(): Boolean {
        return Pattern.matches("application/.*xml", mimeType())
    }

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        val XMLREGEXP by lazy { Regex("^\\s*<\\?xml\\s*version.*") }
        val HTMLREGEXP by lazy { Regex("^\\s*(<!DOCTYPE)|(<HTML>).*") }
        val JSONREGEXP by lazy { Regex("^\\s*(true|false|null|[0-9]+|\"\\w*|\\{\\s*(\\}|\"\\w+)|\\[\\s*).*") }
        val XMLREGEXP2 by lazy { Regex("^\\s*<\\w+\\s*(:\\w+=[\"”][^\"”]+[\"”])?.*") }
    }
}