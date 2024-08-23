package au.com.dius.pact.matchers

import org.apache.http.entity.ContentType

object MatchingConfig {

    private val xmlRegex = Regex("application/.*(xml|XML).*")
    private val jsonRegex = Regex("application/.*(json|JSON).*")
    private val imageRegex = Regex("image/.*")

    private var bodyMatchers = mutableMapOf<Regex, BodyMatcher>()

    init {
        setDefaultBodyMatchers()
    }

    fun lookupBodyMatcher(mimeType: String): BodyMatcher {
        return bodyMatchers.entries.firstOrNull { entry -> mimeType.matches(entry.key) }?.value ?: PlainTextBodyMatcher()
    }

    fun isJson(contentType: String): Boolean {
        return jsonRegex.matches(contentType)
    }

    fun isXml(contentType: String): Boolean {
        return xmlRegex.matches(contentType)
    }

    fun clearBodyMatchers() {
        bodyMatchers.clear()
    }

    fun setDefaultBodyMatchers() {
        // TODO: bodyMatchers[xmlRegex] = XmlBodyMatcher()
        bodyMatchers[jsonRegex] = JsonBodyMatcher()
        bodyMatchers[Regex(ContentType.APPLICATION_JSON_RPC.mimeType)] = JsonBodyMatcher()
        bodyMatchers[Regex(ContentType.APPLICATION_JSONREQUEST.mimeType)] = JsonBodyMatcher()
        bodyMatchers[Regex(ContentType.TEXT_PLAIN.mimeType)] = PlainTextBodyMatcher()
        bodyMatchers[Regex(ContentType.DEFAULT_BINARY.mimeType)] = BinaryBodyMatcher()
        bodyMatchers[imageRegex] = BinaryBodyMatcher()
    }

    fun setBodyMatcher(mimeTypeRegex: Regex, matcher: BodyMatcher) {
        bodyMatchers[mimeTypeRegex] = matcher
    }
}