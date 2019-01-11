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
            headers[contentTypeKey]!!.split(regex=CONTENT_TYPE_REGEXP).first()
        } else {
            detectContentType()
        }
    }

    fun detectContentType(): String {
        return if (body.isPresent()) {
            val s = body.value!!.substring(0,Math.min(body.value!!.length, 32)).filter { it != '\n' }
            if (Pattern.matches(XMLREGEXP, s)) {
                "application/xml"
            } else if (Pattern.matches(HTMLREGEXP, s.toUpperCase())) {
                "text/html"
            } else if (Pattern.matches(JSONREGEXP, s)) {
                "application/json"
            } else if (Pattern.matches(XMLREGEXP2, s)) {
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
        val CONTENT_TYPE_REGEXP by lazy { Pattern.compile("\\s*;\\s*") }
        val XMLREGEXP = "^\\s*<\\?xml\\s*version.*"
        val HTMLREGEXP = "^\\s*(<!DOCTYPE)|(<HTML>).*"
        val JSONREGEXP = "^\\s*(true|false|null|[0-9]+|\"\\w*|\\{\\s*(}|\"\\w+)|\\[\\s*).*"
        val XMLREGEXP2 = "^\\s*<\\w+\\s*(:\\w+=[\"”][^\"”]+[\"”])?.*"
    }
}