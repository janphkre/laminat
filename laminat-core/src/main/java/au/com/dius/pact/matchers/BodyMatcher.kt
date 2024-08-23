package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.matchingrules.MatchingRules

abstract class BodyMatcher {

    fun matchBody(expected: Request, actual: IncomingRequest, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        return when (expected.body) {
            OptionalBody.MissingBody -> listOf(RequestMatchProblem.None)
            OptionalBody.NullBody, OptionalBody.EmptyBody -> {
                if (actual.getBodySize() > 0) {
                    val actualBodyString = actual.getBody()
                    listOf(RequestMatchProblem.BodyMismatch("Expected empty body but received '$actualBodyString'"))
                } else {
                    listOf(RequestMatchProblem.None)
                }
            }
            else -> {
                if (actual.getBodySize() <= 0) {
                    listOf(RequestMatchProblem.BodyMismatch("Expected body '${expected.body}' but was missing"))
                } else {
                    matchContent(expected.body, actual, expected.matchingRules, allowUnexpectedKeys)
                }
            }
        }
    }

    protected abstract fun matchContent(expected: OptionalBody, actual: IncomingRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem>
}