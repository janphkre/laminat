package au.com.dius.pact.matchers

import au.com.dius.pact.external.IncomingRequest
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.requests.OptionalBody
import au.com.dius.pact.model.util.json.JsonElement
import kotlin.math.min

class JsonBodyMatcher : BodyMatcher() {

    override fun matchContent(
        expected: OptionalBody,
        actual: IncomingRequest,
        matchers: MatchingRules,
        allowUnexpectedKeys: Boolean
    ): List<RequestMatchProblem> {

        val path = listOf("$")

        val actualJson = actual.getBodyAsJson()
        val expectedJson = expected.parsedBodyAsJson

        return matchJsonElement(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
    }

    private fun matchJsonElement(
        path: List<String>,
        expectedJson: JsonElement?,
        actualJson: JsonElement?,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        return if (expectedJson is JsonElement.Array && actualJson is JsonElement.Array) {
            matchJsonArray(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if (expectedJson is JsonElement.Object && actualJson is JsonElement.Object) {
            matchJsonObject(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if (expectedJson is JsonElement.Primitive && actualJson is JsonElement.Primitive) {
            matchJsonPrimitive(path, expectedJson, actualJson, matchers)
        } else if (expectedJson is JsonElement.Null && actualJson is JsonElement.Null) {
            matchJsonNull()
        } else if (expectedJson == null && actualJson != null && !allowUnexpectedKeys) {
            listOf(RequestMatchProblem.BodyMismatch("Received unexpected element $actualJson"))
        } else {
            listOf(RequestMatchProblem.BodyMismatch("Expected element '$expectedJson' but received '$actualJson'"))
        }
    }

    private fun matchJsonArray(
        path: List<String>,
        expectedJson: JsonElement.Array,
        actualJson: JsonElement.Array,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val category = Matchers.definedMatchers("body", path, matchers)
        return if (category?.isNotEmpty() == true) {
            val problems = if (Matchers.definedWildcardMatchers( "body", path.plus("any"), matchers)) {
                Matchers.doMatch(category, path, expectedJson, actualJson, MismatchFactory.BodyMismatchFactory)
            } else {
                emptyList<RequestMatchProblem>()
            }
            if (expectedJson.size() != 0) {
                val paddedExpectedValues = Array(min(actualJson.size() - expectedJson.size(), 0)) { expectedJson.first() }
                problems.plus(matchJsonArrayContent(expectedJson.plus(elements = paddedExpectedValues), actualJson, path, allowUnexpectedKeys, matchers))
            } else {
                problems
            }
        } else {
            if (expectedJson.size() != 0 && actualJson.size() == 0) {
                listOf(RequestMatchProblem.BodyMismatch("Expected an empty List but received $actualJson",
                path.joinToString(".")))
            } else {
                var problems = matchJsonArrayContent(expectedJson, actualJson, path, allowUnexpectedKeys, matchers)
                if (expectedJson.size() != actualJson.size()) {
                    problems = problems.plus(RequestMatchProblem.BodyMismatch(
                        "Expected a List with ${expectedJson.size()} elements but received ${actualJson.size()} elements",
                        path.joinToString(".")))
                }
                problems
            }
        }
    }

    private fun matchJsonArrayContent(
        expectedJson: Iterable<JsonElement>,
        actualJson: JsonElement.Array,
        path: List<String>,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val problems = mutableListOf<RequestMatchProblem>()
        expectedJson.forEachIndexed { index, expectedElement ->
            if (index < actualJson.size()) {
                problems.addAll(matchJsonElement(path.plus(index.toString()), expectedElement, actualJson.get(index), allowUnexpectedKeys, matchers))
            } else if (Matchers.definedMatchers("body", path, matchers)?.isNotEmpty() != true) {
                problems.add(RequestMatchProblem.BodyMismatch(
                    "Expected $expectedElement but was missing",
                    path.joinToString(".")))
            }
        }
        return problems
    }

    private fun matchJsonObject(
        path: List<String>,
        expectedJson: JsonElement.Object,
        actualJson: JsonElement.Object,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedEntrySet = expectedJson.entries
        val actualEntrySet = actualJson.entries
        return if (expectedEntrySet.isEmpty() && actualEntrySet.isNotEmpty()) {
            listOf(RequestMatchProblem.BodyMismatch(
                "Expected an empty Map but received '$actualJson'",
                path.joinToString(".")))
        } else {
            val problems = mutableListOf<RequestMatchProblem>()
            if (allowUnexpectedKeys && expectedEntrySet.size > actualEntrySet.size) {
                problems.add(RequestMatchProblem.BodyMismatch(
                    "Expected a Map with at least ${expectedEntrySet.size} elements but received ${actualEntrySet.size} elements",
                    path.joinToString(".")))
            } else if (!allowUnexpectedKeys && expectedEntrySet.size != actualEntrySet.size) {
                problems.add(RequestMatchProblem.BodyMismatch(
                "Expected a Map with ${expectedEntrySet.size} elements but received ${actualEntrySet.size} elements",
                path.joinToString(".")))
            }
            if (Matchers.definedWildcardMatchers( "body", path.plus("any"), matchers)) {
                actualEntrySet.forEach { entry ->
                    val expectedValue = expectedJson.get(entry.key)
                    if (expectedValue != null || !allowUnexpectedKeys) {
                        problems.addAll(matchJsonElement(path.plus(entry.key), expectedValue, entry.value, allowUnexpectedKeys, matchers))
                    }
                }
            } else {
                expectedEntrySet.forEach { entry ->
                    val actualValue = actualJson.get(entry.key)
                    if (actualValue != null) {
                        problems.addAll(matchJsonElement(path.plus(entry.key), entry.value, actualValue, allowUnexpectedKeys, matchers))
                    } else {
                        problems.add(RequestMatchProblem.BodyMismatch(
                            "Expected ${entry.key}=${entry.value} but was missing",
                            path.joinToString(".")))
                    }
                }
            }
            problems
        }
    }

    private fun matchJsonPrimitive(
        path: List<String>,
        expectedJson: JsonElement.Primitive,
        actualJson: JsonElement.Primitive,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedValue = expectedJson.getValue()
        val actualValue = actualJson.getValue()
        val category = Matchers.definedMatchers("body", path, matchers)
        return if (category?.isNotEmpty() == true) {
            Matchers.doMatch(category, path, expectedValue, actualValue, MismatchFactory.BodyMismatchFactory)
        } else {
            if (expectedValue == actualValue) {
                listOf(RequestMatchProblem.None)
            } else {
                listOf(RequestMatchProblem.BodyMismatch(
                    "Expected $expectedValue but received $actualValue",
                    path.joinToString(".")))
            }
        }
    }

    private fun matchJsonNull(): List<RequestMatchProblem> {
        return listOf(RequestMatchProblem.None)
    }
}