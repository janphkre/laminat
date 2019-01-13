package au.com.dius.pact.matchers

import au.com.dius.pact.model.Request
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

internal object Matching {

    fun matchMethod(expectedMethod: String, actualMethod: String?): RequestMatchProblem {
        return if(expectedMethod.equals(actualMethod, true)) {
            RequestMatchProblem.None
        }
        else {
            RequestMatchProblem.MethodMismatch(expectedMethod, actualMethod)
        }
    }

    fun matchPath(expected: Request, actual: RecordedRequest): RequestMatchProblem {
        val matchers = Matchers.definedMatchers("path", emptyList(), expected.matchingRules)
        return if (matchers?.isNotEmpty() == true) {
            val mismatch = Matchers.doMatch(
                matchers,
                emptyList(),
                expected.path,
                actual.path,
                MismatchFactory.PathMismatchFactory
            )
            mismatch.firstOrNull() ?: RequestMatchProblem.None
        }
        else if(expected.path == actual.path || actual.path.matches(Regex(expected.path))){
            RequestMatchProblem.None
        }
        else {
            RequestMatchProblem.PathMismatch(expected.path, actual.path)
        }
    }

    fun matchQuery(expected: Request, actual: RecordedRequest): List<RequestMatchProblem> {
        val problems = LinkedList<RequestMatchProblem.QueryMismatch>()
        expected.query.entries.forEach { expectedEntry ->
            val actualValues = actual.requestUrl.queryParameterValues(expectedEntry.key)
            if(actualValues == null) {
                problems.add(
                    RequestMatchProblem.QueryMismatch("Expected query parameter '${expectedEntry.key}' but was missing", "$.query.${expectedEntry.key}"))
            } else {
                problems.addAll(QueryMatcher.compareQuery(expectedEntry.key, expectedEntry.value, actualValues, expected.matchingRules))
            }
        }
        actual.requestUrl.queryParameterNames().forEach { actualName ->
            if(expected.query.get(actualName) == null) {
                problems.add(
                    RequestMatchProblem.QueryMismatch("Unexpected query parameter  '$actualName' received", "$.query.$actualName"))
            }
        }
        return problems
    }

    fun matchRequestHeaders(expected: Request, actual: RecordedRequest): List<RequestMatchProblem> {
        val problems = LinkedList<RequestMatchProblem>()
        val expectedWithoutCookies = expected.headersWithoutCookie()
        val actualWithoutCookies = actual.headers.toMultimap().filterKeys { it.toLowerCase() != "cookie" }
        expectedWithoutCookies.forEach { expectedEntry ->
            val actualValue = actualWithoutCookies.get(expectedEntry.key)
            if(actualValue == null) {
                problems.add(RequestMatchProblem.HeaderMismatch(expectedEntry.key, "Expected a header '${expectedEntry.key}' but was missing"))

            } else {
                problems.addAll(
                    HeaderMatcher.compareHeader(
                        expectedEntry.key,
                        expectedEntry.value,
                        actualValue,
                        expected.matchingRules
                    )
                )
            }
        }
        return problems
    }

    fun matchCookie(expectedCookie: List<String>?, actualCookie: List<String>?): RequestMatchProblem {
        return if(actualCookie?.containsAll(expectedCookie ?: emptyList()) == true) {
            RequestMatchProblem.None
        } else {
            RequestMatchProblem.CookieMismatch(expectedCookie, actualCookie)
        }
    }

    fun matchBody(expected: Request, actual: RecordedRequest, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val expectedMimeType = expected.mimeType()
        val actualMimeType = actual.getHeader("Content-Type")
        return if (expectedMimeType == actualMimeType) {
            MatchingConfig.lookupBodyMatcher(actualMimeType).matchBody(expected, actual, allowUnexpectedKeys)
        } else {
            if (!expected.body.isPresent()) {
                emptyList()
            }
            else {
                listOf(RequestMatchProblem.BodyTypeMismatch(expectedMimeType, actualMimeType))
            }
        }
    }
}