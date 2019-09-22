package au.com.dius.pact.matchers

import au.com.dius.pact.model.Request
import okhttp3.mockwebserver.RecordedRequest
import org.apache.http.entity.ContentType
import java.util.LinkedList

internal object Matching {

    fun matchMethod(expectedMethod: String, actualMethod: String?): List<RequestMatchProblem> {
        return if (expectedMethod.equals(actualMethod, true) != false) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.MethodMismatch(expectedMethod, actualMethod))
        }
    }

    fun matchPath(expected: Request, actual: RecordedRequest): List<RequestMatchProblem> {
        val matchers = Matchers.definedMatchers("path", emptyList(), expected.matchingRules)
        val actualPath = actual.requestUrl.encodedPath().split('?').firstOrNull()
        return if (matchers?.isNotEmpty() == true) {
            Matchers.doMatch(
                matchers,
                emptyList(),
                expected.path,
                actualPath,
                MismatchFactory.PathMismatchFactory)
        } else if (expected.path == actualPath || actualPath?.matches(Regex(expected.path)) == true) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.PathMismatch(expected.path, actualPath))
        }
    }

    fun matchQuery(expected: Request, actual: RecordedRequest): List<RequestMatchProblem> {
        val problems = LinkedList<RequestMatchProblem>()
        expected.query.entries.forEach { expectedEntry ->
            val actualValues = actual.requestUrl.queryParameterValues(expectedEntry.key)
            if (actualValues == null) {
                problems.add(RequestMatchProblem.QueryMismatch(
                    "Expected query parameter '${expectedEntry.key}' but was missing",
                    "$.query.${expectedEntry.key}"))
            } else {
                problems.addAll(QueryMatcher.compareQuery(expectedEntry.key, expectedEntry.value, actualValues, expected.matchingRules))
            }
        }
        //DO WE REALLY WANT TO FAIL ON UNMATCHED QUERY PARAMETERS? Apparently if we want to meet pact-spec.
        actual.requestUrl.queryParameterNames().forEach { actualName ->
            if (expected.query[actualName] == null) {
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
            val actualValue = actualWithoutCookies[expectedEntry.key.toLowerCase()]
            if (actualValue == null) {
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

    fun matchCookie(expectedCookie: List<String>?, actualCookie: List<String>?): List<RequestMatchProblem> {
        return if (actualCookie?.containsAll(expectedCookie ?: emptyList()) == true) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.CookieMismatch(expectedCookie, actualCookie))
        }
    }

    fun matchBody(expected: Request, actual: RecordedRequest, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val expectedMimeType = expected.mimeType()
        val actualMimeType = actual.getHeader(ContentType.CONTENT_TYPE) ?: ""
        return if (actualMimeType.split(';').contains(expectedMimeType)) {
            MatchingConfig.lookupBodyMatcher(expectedMimeType).matchBody(expected, actual, allowUnexpectedKeys)
        } else {
            if (!expected.body.isPresent()) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(RequestMatchProblem.BodyTypeMismatch(expectedMimeType, actualMimeType))
            }
        }
    }
}