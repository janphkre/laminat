package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher

class PlainTextBodyMatcher : BodyMatcher() {

    override fun matchContent(expected: OptionalBody, actual: IncomingRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val regex = matchers.getCategory("body")?.matchingRules?.get("$")
        val actualString = actual.getBody()
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