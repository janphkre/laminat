package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher

// TODO CHECK IF MATCHER NEEDS TO BE MOVED LOGIC WISE
class PlainTextBodyMatcher : BodyMatcher() {

    override fun matchContent(expected: OptionalBody, actual: IncomingRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        if (expected !is OptionalBody.StringBody) {
            return listOf(RequestMatchProblem.BodyMismatch("Expected body is not a string body!", "$"))
        }

        val regex = matchers.getCategory("body")?.matchingRules?.get("$")
        val actualBody = actual.getBodyAsString()

        return if (regex?.rules?.get(0) !is RegexMatcher) {
            if (expected.unwrap() == actualBody) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(RequestMatchProblem.BodyMismatch("Expected body '$expected' to match '$actual' using equality but did not match"))
            }
        } else if (actual.getBodyAsString()?.matches((regex.rules[0] as RegexMatcher).regex) == true) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.BodyMismatch("Expected body '$expected' to match '$actual' using regex '$regex' but did not match"))
        }
    }
}