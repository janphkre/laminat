package au.com.dius.pact.matchers

import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.matchingrules.MatchingRules
import okhttp3.mockwebserver.RecordedRequest

abstract class BodyMatcher {

    fun matchBody(expected: Request, actual: RecordedRequest, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val actualBodyString = actual.body?.readUtf8()
        return when(expected.body.state) {
            OptionalBody.State.MISSING -> listOf(RequestMatchProblem.None)
            OptionalBody.State.NULL, OptionalBody.State.EMPTY -> {
                if(actualBodyString.isNullOrEmpty()) {
                    listOf(RequestMatchProblem.BodyMismatch("Expected empty body but received '$actualBodyString'"))
                } else {
                    listOf(RequestMatchProblem.None)
                }
            }
            else -> {
                if(actualBodyString.isNullOrEmpty()) {
                    listOf(RequestMatchProblem.BodyMismatch("Expected body '${expected.body.value}' but was missing"))
                } else {
                    matchContent(expected.body, actual, expected.matchingRules, allowUnexpectedKeys)
                }
            }
        }
    }

    protected abstract fun matchContent(expected: OptionalBody, actual: RecordedRequest, matchers: MatchingRules, allowUnexpectedKeys: Boolean): List<RequestMatchProblem>
}