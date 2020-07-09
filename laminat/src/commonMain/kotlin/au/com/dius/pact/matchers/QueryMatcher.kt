package au.com.dius.pact.matchers

import au.com.dius.pact.model.matchingrules.MatchingRules

object QueryMatcher {

    private fun compare(parameter: String, path: List<String>, expected: String, actual: String, matchers: MatchingRules): List<RequestMatchProblem> {
        val category = Matchers.definedMatchers("query", path, matchers)
        return if (category?.isNotEmpty() == true) {
            Matchers.doMatch(
                category,
                path,
                expected,
                actual,
                MismatchFactory.QueryMismatchFactory
            )
        } else {
            if (expected == actual) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(
                    RequestMatchProblem.QueryMismatch(
                        "Expected '$expected' but received '$actual' for query parameter '$parameter'",
                        parameter
                    )
                )
            }
        }
    }

    private fun compareQueryParameterValues(parameter: String, expected: List<String>, actual: List<String>, path: List<String>, matchers: MatchingRules): List<RequestMatchProblem> {
        val result = mutableListOf<RequestMatchProblem>()
        expected.forEachIndexed { index, item ->
            if (index < actual.size) {
                result.addAll(
                    compare(
                        parameter,
                        path.plus(index.toString()),
                        item,
                        actual[index],
                        matchers
                    )
                )
            } else if (Matchers.definedMatchers("query", path, matchers)?.isEmpty() != false) {
                    result.add(
                        RequestMatchProblem.QueryMismatch(
                            "Expected query parameter $parameter but was missing",
                            path.joinToString(".")
                        )
                    )
            }
        }
        return result
    }

    fun compareQuery(parameter: String, expected: List<String>, actual: List<String>, matchers: MatchingRules): List<RequestMatchProblem> {
        val result = mutableListOf<RequestMatchProblem>()
        val path = listOf(parameter)
        val category = Matchers.definedMatchers("query", path, matchers)
        if (category?.isNotEmpty() == true) {
            expected.zip(actual).forEach {
                result.addAll(
                    Matchers.doMatch(
                        category,
                        path,
                        it.first,
                        it.second,
                        MismatchFactory.QueryMismatchFactory
                    )
                )
            }
            result.addAll(
                compareQueryParameterValues(
                    parameter,
                    expected,
                    actual,
                    path,
                    matchers
                )
            )
        } else {
            if (expected.isEmpty() && actual.isNotEmpty()) {
                result.add(
                    RequestMatchProblem.QueryMismatch(
                        "Expected an empty parameter List for $parameter but received $actual",
                        parameter
                    )
                )
            } else {
                if (expected.size != actual.size) {
                    result.add(
                        RequestMatchProblem.QueryMismatch(
                            "Expected query parameter $parameter with ${expected.size} values but received ${actual.size} values",
                            parameter
                        )
                    )
                }
                result.addAll(
                    compareQueryParameterValues(
                        parameter,
                        expected,
                        actual,
                        path,
                        matchers
                    )
                )
            }
        }
        return result
    }
}