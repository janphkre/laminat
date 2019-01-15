package au.com.dius.pact.matchers

object MatchingConfig {
    private var bodyMatchers = mapOf<Regex, BodyMatcher>(
        // TODO: Pair(Regex("application/.*xml"), XmlBodyMatcher()),
        Pair(Regex("application/.*json"), JsonBodyMatcher()),
        Pair(Regex("application/json-rpc"), JsonBodyMatcher()),
        Pair(Regex("application/jsonrequest"), JsonBodyMatcher()),
        Pair(Regex("text/plain"), PlainTextBodyMatcher()))

    fun lookupBodyMatcher(mimeType: String): BodyMatcher {
        return bodyMatchers.entries.firstOrNull{ entry -> mimeType.matches(entry.key)}?.value ?: PlainTextBodyMatcher()
    }
}