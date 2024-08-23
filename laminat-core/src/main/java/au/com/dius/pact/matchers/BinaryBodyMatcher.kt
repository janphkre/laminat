package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules

class BinaryBodyMatcher : BodyMatcher() {

    override fun matchContent(expected: OptionalBody, actual: IncomingRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        if (expected !is OptionalBody.BinaryBody) {
            return listOf(RequestMatchProblem.BodyMismatch("Expected body is not a binary body!"))
        }
        val expectedBody = expected.unwrap()
        val actualBody = actual.getBody()

        if (expectedBody.contentEquals(actualBody)) {
            return listOf(RequestMatchProblem.None)
        }
        return listOf(RequestMatchProblem.BodyMismatch("Failed to match expected body $expectedBody with $actualBody"))
    }
}