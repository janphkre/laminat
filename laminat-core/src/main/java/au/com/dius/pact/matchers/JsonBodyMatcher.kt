package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.util.toReader
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import java.util.LinkedList
import kotlin.math.min

class JsonBodyMatcher : BodyMatcher() {

    override fun matchContent(
        expected: OptionalBody,
        actual: IncomingRequest,
        matchers: MatchingRules,
        allowUnexpectedKeys: Boolean
    ): List<RequestMatchProblem> {
        if (expected !is OptionalBody.StringBody) {
            return listOf(RequestMatchProblem.BodyMismatch("Expected body is not a string body!"))
        }

        val path = listOf("$")

        val actualJson = Json.parse(actual.getBody()?.toReader(actual.getBodyCharset()))
        val expectedJson = expected.unwrapJson()

        return matchJsonElement(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
    }

    private fun matchJsonElement(
        path: List<String>,
        expectedJson: Json?,
        actualJson: Json?,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        return if (expectedJson is Json.Array && actualJson is Json.Array) {
            matchJsonArray(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if (expectedJson is Json.Object && actualJson is Json.Object) {
            matchJsonObject(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if (expectedJson is Json.Primitive && actualJson is Json.Primitive) {
            matchJsonPrimitive(path, expectedJson, actualJson, matchers)
        } else if (expectedJson is Json.Null && actualJson is Json.Null) {
            matchJsonNull()
        } else if (expectedJson == null && actualJson != null && !allowUnexpectedKeys) {
            listOf(RequestMatchProblem.BodyMismatch("Received unexpected element $actualJson"))
        } else {
            listOf(RequestMatchProblem.BodyMismatch("Expected element '$expectedJson' but received '$actualJson'"))
        }
    }

    private fun matchJsonArray(
        path: List<String>,
        expectedJson: Json.Array,
        actualJson: Json.Array,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val category = Matchers.definedMatchers("body", path, matchers)
        return if (category?.isNotEmpty() == true) {
            val problems = if (Matchers.definedWildcardMatchers("body", path.plus("any"), matchers)) {
                Matchers.doMatch(category, path, expectedJson, actualJson, MismatchFactory.BodyMismatchFactory)
            } else {
                emptyList()
            }
            if (expectedJson.size != 0) {
                val paddedExpectedValues = Array(min(actualJson.size - expectedJson.size, 0)) { expectedJson.first() }
                problems.plus(matchJsonArrayContent(expectedJson.plus(elements = paddedExpectedValues), actualJson, path, allowUnexpectedKeys, matchers))
            } else {
                problems
            }
        } else {
            if (expectedJson.size != 0 && actualJson.size == 0) {
                listOf(
                    RequestMatchProblem.BodyMismatch(
                        "Expected an empty List but received $actualJson",
                        path.joinToString(".")
                    )
                )
            } else {
                var problems = matchJsonArrayContent(expectedJson, actualJson, path, allowUnexpectedKeys, matchers)
                if (expectedJson.size != actualJson.size) {
                    problems = problems.plus(
                        RequestMatchProblem.BodyMismatch(
                            "Expected a List with ${expectedJson.size} elements but received ${actualJson.size} elements",
                            path.joinToString(".")
                        )
                    )
                }
                problems
            }
        }
    }

    private fun matchJsonArrayContent(
        expectedJson: Iterable<Json>,
        actualJson: Json.Array,
        path: List<String>,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val problems = LinkedList<RequestMatchProblem>()
        expectedJson.forEachIndexed { index, expectedElement ->
            if (index < actualJson.size) {
                problems.addAll(matchJsonElement(path.plus(index.toString()), expectedElement, actualJson[index], allowUnexpectedKeys, matchers))
            } else if (Matchers.definedMatchers("body", path, matchers)?.isNotEmpty() != true) {
                problems.add(
                    RequestMatchProblem.BodyMismatch(
                        "Expected $expectedElement but was missing",
                        path.joinToString(".")
                    )
                )
            }
        }
        return problems
    }

    private fun matchJsonObject(
        path: List<String>,
        expectedJson: Json.Object,
        actualJson: Json.Object,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        return if (expectedJson.isEmpty() && actualJson.isNotEmpty()) {
            listOf(
                RequestMatchProblem.BodyMismatch(
                    "Expected an empty Map but received '$actualJson'",
                    path.joinToString(".")
                )
            )
        } else {
            val problems = LinkedList<RequestMatchProblem>()
            if (allowUnexpectedKeys && expectedJson.size > actualJson.size) {
                problems.add(
                    RequestMatchProblem.BodyMismatch(
                        "Expected a Map with at least ${expectedJson.size} elements but received ${actualJson.size} elements",
                        path.joinToString(".")
                    )
                )
            } else if (!allowUnexpectedKeys && expectedJson.size != actualJson.size) {
                problems.add(
                    RequestMatchProblem.BodyMismatch(
                        "Expected a Map with ${expectedJson.size} elements but received ${actualJson.size} elements",
                        path.joinToString(".")
                    )
                )
            }
            if (Matchers.definedWildcardMatchers("body", path.plus("any"), matchers)) {
                actualJson.forEach { entry ->
                    if (expectedJson.containsKey(entry.key) || !allowUnexpectedKeys) {
                        val expectedValue = expectedJson[entry.key]
                        problems.addAll(matchJsonElement(path.plus(entry.key), expectedValue, entry.value, allowUnexpectedKeys, matchers))
                    }
                }
            } else {
                expectedJson.forEach { entry ->
                    if (actualJson.containsKey(entry.key)) {
                        val actualValue = actualJson[entry.key]
                        problems.addAll(matchJsonElement(path.plus(entry.key), entry.value, actualValue, allowUnexpectedKeys, matchers))
                    } else {
                        problems.add(
                            RequestMatchProblem.BodyMismatch(
                                "Expected ${entry.key}=${entry.value} but was missing",
                                path.joinToString(".")
                            )
                        )
                    }
                }
            }
            problems
        }
    }

    private fun matchJsonPrimitive(
        path: List<String>,
        expectedJson: Json.Primitive,
        actualJson: Json.Primitive,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedValue = expectedJson.unwrap<Any>()
        val actualValue = actualJson.unwrap<Any>()
        val category = Matchers.definedMatchers("body", path, matchers)
        return if (category?.isNotEmpty() == true) {
            Matchers.doMatch(category, path, expectedValue, actualValue, MismatchFactory.BodyMismatchFactory)
        } else {
            if (expectedValue == actualValue) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(
                    RequestMatchProblem.BodyMismatch(
                        "Expected $expectedValue but received $actualValue",
                        path.joinToString(".")
                    )
                )
            }
        }
    }

    private fun matchJsonNull(): List<RequestMatchProblem> {
        return listOf(RequestMatchProblem.None)
    }
}