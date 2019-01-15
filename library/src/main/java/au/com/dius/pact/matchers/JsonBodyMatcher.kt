package au.com.dius.pact.matchers

import au.com.dius.pact.external.parseBodyToJson
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import okhttp3.mockwebserver.RecordedRequest
import java.lang.UnsupportedOperationException
import java.util.*

class JsonBodyMatcher : BodyMatcher() {

    override fun matchContent(
        expected: OptionalBody,
        actual: RecordedRequest,
        matchers: MatchingRules,
        allowUnexpectedKeys: Boolean
    ): List<RequestMatchProblem> {

        val path = listOf("$")

        val actualJson = actual.parseBodyToJson()
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
        return if(expectedJson?.isJsonArray == true && actualJson?.isJsonArray == true) {
            matchJsonArray(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if(expectedJson?.isJsonObject == true && actualJson?.isJsonObject == true) {
            matchJsonObject(path, expectedJson, actualJson, allowUnexpectedKeys, matchers)
        } else if(expectedJson?.isJsonPrimitive == true && actualJson?.isJsonPrimitive == true) {
            matchJsonPrimitive(path, expectedJson, actualJson, matchers)
        } else if(expectedJson?.isJsonNull == true && actualJson?.isJsonNull != false) {
            matchJsonNull()
        } else if(expectedJson == null && actualJson != null && !allowUnexpectedKeys) {
            listOf(RequestMatchProblem.BodyMismatch("Received unexpected element $actualJson"))
        } else {
            listOf(RequestMatchProblem.BodyMismatch("Expected element '$expectedJson' but received '$actualJson'"))
        }
    }

    private fun matchJsonArray(
        path: List<String>,
        expectedJson: JsonElement,
        actualJson: JsonElement,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedValues = expectedJson.asJsonArray
        val actualValues = actualJson.asJsonArray
        val category = Matchers.definedMatchers("body", path, matchers)
        return if (category?.isNotEmpty() == true) {
            //TODO: DoMatch on JSONArray?!
            var problems = Matchers.doMatch(category, path, expectedValues, actualValues, MismatchFactory.BodyMismatchFactory)
            if (expectedValues.size() != 0) {
                val paddedExpectedValues = Array(Math.min(actualValues.size() - expectedValues.size(), 0)) { expectedValues.first() }
                problems = problems.plus(matchJsonArrayContent(expectedValues.plus(elements=paddedExpectedValues), actualValues, path, allowUnexpectedKeys, matchers))
            }
            problems
        } else {
            if (expectedValues.size() != 0 && actualValues.size() == 0) {
                listOf(RequestMatchProblem.BodyMismatch("Expected an empty List but received $actualValues",
                path.joinToString(".")))
            } else {
                var problems = matchJsonArrayContent(expectedValues, actualValues, path, allowUnexpectedKeys, matchers)
                if (expectedValues.size() != actualValues.size()) {
                    problems = problems.plus(RequestMatchProblem.BodyMismatch(
                        "Expected a List with ${expectedValues.size()} elements but received ${actualValues.size()} elements",
                        path.joinToString(".")))
                }
                problems
            }
        }
    }

    private fun matchJsonArrayContent(
        expectedJson: Iterable<JsonElement>,
        actualJson: JsonArray,
        path: List<String>,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val problems = LinkedList<RequestMatchProblem>()
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
        expectedJson: JsonElement,
        actualJson: JsonElement,
        allowUnexpectedKeys: Boolean,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedObject = expectedJson.asJsonObject
        val actualObject = actualJson.asJsonObject
        val expectedEntrySet = expectedObject.entrySet()
        val actualEntrySet = actualObject.entrySet()
        return if (expectedEntrySet.isEmpty() && actualEntrySet.isNotEmpty()) {
            listOf(RequestMatchProblem.BodyMismatch(
                "Expected an empty Map but received '$actualJson'",
                path.joinToString(".")))
        } else {
            val problems = LinkedList<RequestMatchProblem>()
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
                    val expectedValue = expectedObject.get(entry.key)
                    if (expectedValue != null || !allowUnexpectedKeys) {
                        problems.addAll(matchJsonElement(path.plus(entry.key), expectedValue,  entry.value, allowUnexpectedKeys, matchers))
                    }
                }
            } else {
                expectedEntrySet.forEach{ entry ->
                    val actualValue = actualObject.get(entry.key)
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
        expectedJson: JsonElement,
        actualJson: JsonElement,
        matchers: MatchingRules
    ): List<RequestMatchProblem> {
        val expectedValue = expectedJson.asJsonPrimitive.getValue()
        val actualValue = actualJson.asJsonPrimitive.getValue()
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

    private fun JsonPrimitive.getValue(): Any? {
        return when {
            isBoolean -> asBoolean
            isString -> asString
            isNumber -> asNumber
            else -> throw UnsupportedOperationException("Unknown Json Primitive Type!")
        }
    }

    private fun matchJsonNull(): List<RequestMatchProblem> {
        return listOf(RequestMatchProblem.None)
    }


}
