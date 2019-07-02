package au.com.dius.pact.matchers

import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher
import okhttp3.mockwebserver.RecordedRequest

class PlainTextBodyMatcher : BodyMatcher() {

    override fun matchContent(expected: OptionalBody, actual: RecordedRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val regex = matchers.getCategory("body")?.matchingRules?.get("$")
        val actualString = actual.body?.readUtf8()
        return if (regex?.rules?.get(0) !is RegexMatcher) {
            if (expected.value == actualString) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(RequestMatchProblem.BodyMismatch("Expected body '$expected' to match '$actual' using equality but did not match"))
            }
        } else if (actualString?.matches((regex.rules[0] as RegexMatcher).regex) == true) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.BodyMismatch("Expected body '$expected' to match '$actual' using regex '$regex' but did not match"))
        }
    }
}