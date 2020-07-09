package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.requests.Request

internal object Matching {

    fun matchMethod(expectedMethod: String, actualMethod: String?): List<RequestMatchProblem> {
        return if (expectedMethod.equals(actualMethod, true)) {
            listOf(RequestMatchProblem.None)
        } else {
            listOf(RequestMatchProblem.MethodMismatch(expectedMethod, actualMethod))
        }
    }

    fun matchPath(expected: Request, actual: IncomingRequest): List<RequestMatchProblem> {
        val matchers = Matchers.definedMatchers("path", emptyList(), expected.matchingRules)
        val actualPath = actual.getEncodedPath().split('?').firstOrNull()
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

    fun matchQuery(expected: Request, actual: IncomingRequest): List<RequestMatchProblem> {
        val problems = mutableListOf<RequestMatchProblem>()
        expected.query.entries.forEach { expectedEntry ->
            val actualValues = actual.queryParameterValues(expectedEntry.key)
            if (actualValues.isEmpty()) {
                problems.add(RequestMatchProblem.QueryMismatch(
                    "Expected query parameter '${expectedEntry.key}' but was missing",
                    "$.query.${expectedEntry.key}"))
            } else {
                problems.addAll(QueryMatcher.compareQuery(expectedEntry.key, expectedEntry.value, actualValues, expected.matchingRules))
            }
        }
        //DO WE REALLY WANT TO FAIL ON UNMATCHED QUERY PARAMETERS? Apparently if we want to meet pact-spec.
        actual.queryParameterNames().forEach { actualName ->
            if (expected.query[actualName] == null) {
                problems.add(
                    RequestMatchProblem.QueryMismatch("Unexpected query parameter  '$actualName' received", "$.query.$actualName"))
            }
        }
        return problems
    }

    fun matchRequestHeaders(expected: Request, actual: IncomingRequest): List<RequestMatchProblem> {
        val problems = mutableListOf<RequestMatchProblem>()
        val expectedWithoutCookies = expected.headersWithoutCookie()
        val actualWithoutCookies = actual.getHeaders().filterKeys { it.toLowerCase() != "cookie" }
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

    fun matchBody(expected: Request, actual: IncomingRequest, allowUnexpectedKeys: Boolean): List<RequestMatchProblem> {
        val expectedMimeType = expected.mimeType()
        val actualMimeType = actual.getContentType()
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