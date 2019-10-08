package au.com.dius.pact.matchers

import au.com.dius.pact.model.matchingrules.MatchingRules
import org.apache.http.entity.ContentType

object HeaderMatcher {

    private fun stripWhiteSpaceAfterCommas(input: String?): String? {
        return input?.replace(Regex(",[ ]*"), ",")
    }

    fun compareHeader(headerKey: String, expected: String, actualValue: List<String>?, matchingRules: MatchingRules): List<RequestMatchProblem> {
        if (actualValue?.size ?: 0 > 1) {
            return listOf(RequestMatchProblem.HeaderMismatch(headerKey, "Expected header '$headerKey' to have only a single value but received ${actualValue?.size} values"))
        }
        val path = listOf(headerKey)
        val category = Matchers.definedMatchers("header", path, matchingRules)
        val actual = actualValue?.firstOrNull()
        return when {
            category?.isNotEmpty() == true -> Matchers.doMatch(category, path, expected, actual, MismatchFactory.HeaderMismatchFactory)
            headerKey.equals(ContentType.CONTENT_TYPE, true) -> matchContentType(expected, actual)
            stripWhiteSpaceAfterCommas(expected) == stripWhiteSpaceAfterCommas(actual) -> listOf(RequestMatchProblem.None)
            else -> listOf(RequestMatchProblem.HeaderMismatch(headerKey, "Expected header '$headerKey' to have value '$expected' but was '$actual'"))
        }
    }

    private fun matchContentType(expected: String, actual: String?): List<RequestMatchProblem> {
        val expectedValues = expected.split(';').map { it.trim() }
        val actualValues = actual?.split(';')?.map { it.trim() }
        val expectedContentType = expectedValues.firstOrNull()
        val actualContentType = actualValues?.firstOrNull()
        val expectedParameters = parseParameters(expectedValues.drop(1))
        val actualParameters = parseParameters(actualValues?.drop(1))
        val headerMismatch = RequestMatchProblem.HeaderMismatch(ContentType.CONTENT_TYPE, "Expected header '${ContentType.CONTENT_TYPE}' to have value '$expected' but was '$actual'")

        val problem = if (expectedContentType == actualContentType) {
            expectedParameters.map { entry ->
                if (actualParameters.contains(entry.key)) {
                    if (entry.value == actualParameters[entry.key]) {
                        null
                    } else {
                        headerMismatch
                    }
                } else {
                    headerMismatch
                }
            }.firstOrNull { it == headerMismatch }
        } else {
            headerMismatch
        }

        return if (problem != null) {
            listOf(problem)
        } else {
            listOf(RequestMatchProblem.None)
        }
    }

    private fun parseParameters(values: List<String>?): Map<String, String> {
        return values?.asSequence()?.map { it.split('=').map { split -> split.trim() }
        }?.fold(HashMap()) { map, item ->
            map[item.component1()] = item.component2()
            map
        } ?: emptyMap()
    }
}