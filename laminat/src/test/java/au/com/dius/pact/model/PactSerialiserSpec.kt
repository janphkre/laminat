package au.com.dius.pact.model

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.generators.RandomIntGenerator
import au.com.dius.pact.model.generators.RandomStringGenerator
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.TypeMatcher
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.PrintWriter
import java.io.StringWriter

class PactSerialiserSpec : StringSpec() {

    private val request = Request(
        "GET", "/", PactReader.queryStringToMap("q=p&q=p2&r=s"),
        mapOf("testreqheader" to "testreqheadervalue"), OptionalBody.body("{\"test\":true}")
    )
    private val response = Response(
        200, mapOf("testreqheader" to "testreqheaderval"),
        OptionalBody.body("{\"responsetest\":true}")
    )
    private val provider = Provider("test_provider")
    private val consumer = Consumer("test_consumer")

    private val requestMatchers = MatchingRules().apply {
        addCategory("body").addRule("$.test", TypeMatcher)
    }
    private val requestWithMatchers = Request(
        "GET", "/", PactReader.queryStringToMap("q=p&q=p2&r=s"),
        mapOf("testreqheader" to "testreqheadervalue"), OptionalBody.body("{\"test\":true}"), requestMatchers
    )
    private val responseMatchers = MatchingRules().apply {
        addCategory("body").addRule("$.responsetest", TypeMatcher)
    }
    private val responseWithMatchers = Response(
        200, mapOf("testreqheader" to "testreqheaderval"),
        OptionalBody.body("{\"responsetest\":true}"), responseMatchers
    )
    private val interactionsWithMatcher = RequestResponseInteraction(
        "test interaction with matchers",
        listOf(ProviderState("test state")), requestWithMatchers, responseWithMatchers
    )
    private val pactWithMatchers = RequestResponsePact(provider, consumer, listOf(interactionsWithMatcher))

    private val requestWithGenerators = request.copy().apply {
        generators.addGenerators(Generators(mutableMapOf(Category.BODY to mutableMapOf("a" to RandomIntGenerator(10, 20)))))
    }

    private val responseWithGenerators = response.copy().apply {
        generators.addGenerators(Generators(mutableMapOf(Category.PATH to mutableMapOf("" to RandomStringGenerator(20)))))
    }

    private val interactionsWithGenerators = RequestResponseInteraction(
        "test interaction with generators",
        listOf(ProviderState("test state")), requestWithGenerators, responseWithGenerators
    )
    private val pactWithGenerators = RequestResponsePact(provider, consumer, listOf(interactionsWithGenerators))

    private val requestLowerCaseMethod = Request(
        "get", "/",
        PactReader.queryStringToMap("q=p&q=p2&r=s"),
        mapOf("testreqheader" to "testreqheadervalue"), OptionalBody.body("{\"test\":true}")
    )

    init {

        "PactSerialiser must serialise pact"() {
            // given:
            val sw = StringWriter()
            val testPact = PactParsingUtil.parseAssetToJson("test_pact.json")

            // when:
            PactWriter.writePact(
                RequestResponsePact(
                    Provider("test_provider"), Consumer("test_consumer"),
                    listOf(RequestResponseInteraction("test interaction", listOf(ProviderState("test state")), request, response))
                ),
                PrintWriter(sw), PactSerializationConfig(PactSpecVersion.V3, null)
            )
            val actualPactJson = sw.toString().trim()
            val actualPact = PactParsingUtil.parseContentToJson(actualPactJson)

            // then:
            actualPact shouldBe testPact
        }

        "PactSerialiser must serialise V3 pact"() {
            // given:
            val sw = StringWriter()
            val testPact = PactParsingUtil.parseAssetToJson("test_pact_v3.json")
            val expectedRequest = Request(
                "GET", "/",
                mapOf("q" to listOf("p", "p2"), "r" to listOf("s")), mapOf("testreqheader" to "testreqheadervalue"),
                OptionalBody.body("{\"test\": true}")
            )
            val expectedResponse = Response(
                200, mapOf("testreqheader" to "testreqheaderval"),
                OptionalBody.body("{\"responsetest\" : true}")
            )
            val expectedPact = RequestResponsePact(
                Provider("test_provider"),
                Consumer("test_consumer"), listOf(
                    RequestResponseInteraction(
                        "test interaction", listOf(
                            ProviderState("test state", mapOf("name" to "Testy")),
                            ProviderState("test state 2", mapOf("name" to "Testy2"))
                        ), expectedRequest, expectedResponse
                    )
                )
            )

            // when:
            PactWriter.writePact(expectedPact, PrintWriter(sw), PactSerializationConfig(PactSpecVersion.V3, null))
            val actualPactJson = sw.toString().trim()
            val actualPact = PactParsingUtil.parseContentToJson(actualPactJson)

            // then:
            actualPact shouldBe testPact
        }

        "PactSerialiser must serialise pact with matchers"() {
            // given:
            val sw = StringWriter()
            val testPact = PactParsingUtil.parseAssetToJson("test_pact_matchers.json")

            // when:
            PactWriter.writePact(pactWithMatchers, PrintWriter(sw), PactSerializationConfig(PactSpecVersion.V3, null))
            val actualPactJson = sw.toString().trim()
            val actualPact = PactParsingUtil.parseContentToJson(actualPactJson)

            // then:
            actualPact shouldBe testPact
        }

        "PactSerialiser must convert methods to uppercase"() {
            // given:
            val sw = StringWriter()
            val testPact = PactParsingUtil.parseAssetToJson("test_pact.json")
            val pact = RequestResponsePact(
                Provider("test_provider"), Consumer("test_consumer"),
                listOf(
                    RequestResponseInteraction(
                        "test interaction", listOf(ProviderState("test state")),
                        requestLowerCaseMethod,
                        response
                    )
                )
            )

            // when:
            PactWriter.writePact(pact, PrintWriter(sw), PactSerializationConfig(PactSpecVersion.V3, null))
            val actualPactJson = sw.toString().trim()
            val actualPact = PactParsingUtil.parseContentToJson(actualPactJson)

            // then:
            actualPact shouldBe testPact
        }

        "PactSerialiser must serialise pact with generators"() {
            // given:
            val sw = StringWriter()
            val testPact = PactParsingUtil.parseAssetToJson("test_pact_generators.json")

            // when:
            PactWriter.writePact(pactWithGenerators, PrintWriter(sw), PactSerializationConfig(PactSpecVersion.V3, null))
            val actualPactJson = sw.toString().trim()
            val actualPact = PactParsingUtil.parseContentToJson(actualPactJson)

            // then:
            actualPact shouldBe testPact
        }

        "Correctly handle non-ascii characters"() {
            // given:
            val resultWriter = StringWriter()
            val request = Request(body = OptionalBody.body("\"This is a string with letters ä, ü, ö and ß\""))
            val response = Response(body = OptionalBody.body("\"This is a string with letters ä, ü, ö and ß\""))
            val interaction = RequestResponseInteraction(
                "test interaction with non-ascii characters in bodies",
                emptyList(), request, response
            )
            val pact = RequestResponsePact(
                Provider("test_provider"), Consumer("test_consumer"),
                listOf(interaction)
            )

            // when:
            val writer = PrintWriter(resultWriter)
            PactWriter.writePact(pact, writer, PactSerializationConfig(PactSpecVersion.V2, null))
            writer.close()
            val pactJson = resultWriter.buffer.toString()

            // then:
            pactJson.contains("This is a string with letters ä, ü, ö and ß") shouldBe true
        }
    }
}
