package au.com.dius.pact.matchers

import org.apache.http.entity.ContentType

object MatchingConfig {

    private val xmlRegex = Regex("application/.*xml.*")
    private val jsonRegex = Regex("application/.*json.*")

    private var bodyMatchers = mapOf<Regex, BodyMatcher>(
        // TODO: Pair(xmlRegex, XmlBodyMatcher()),
        Pair(jsonRegex, JsonBodyMatcher()),
        Pair(Regex(ContentType.APPLICATION_JSON_RPC.mimeType), JsonBodyMatcher()),
        Pair(Regex(ContentType.APPLICATION_JSONREQUEST.mimeType), JsonBodyMatcher()),
        Pair(Regex(ContentType.TEXT_PLAIN.mimeType), PlainTextBodyMatcher()))

    fun lookupBodyMatcher(mimeType: String): BodyMatcher {
        return bodyMatchers.entries.firstOrNull { entry -> mimeType.matches(entry.key) }?.value ?: PlainTextBodyMatcher()
    }

    fun isJson(contentType: String): Boolean {
        return jsonRegex.matches(contentType)
    }

    fun isXml(contentType: String): Boolean {
        return xmlRegex.matches(contentType)
    }
}