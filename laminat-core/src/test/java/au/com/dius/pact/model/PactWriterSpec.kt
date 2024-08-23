package au.com.dius.pact.model

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.StringSpecExt
import au.com.dius.pact.external.PactJsonifier
import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.shouldBeException
import io.kotlintest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class PactWriterSpec : StringSpecExt({

    lateinit var pactDir: File

    beforeTest {
        FeatureFlags.restoreDefault(Feature.MERGE_EXISTING_PACTS_FILE)
        pactDir = Files.createTempDirectory("PactWriterSpec").toFile()
    }

    afterTest {
        println("PactDir content:\n${pactDir.list()?.joinToString(separator = "\n")}")
        pactDir.delete()
    }

    afterSpec {
        FeatureFlags.restoreDefault(Feature.MERGE_EXISTING_PACTS_FILE)
    }

    "when writing pacts, do not include optional items that are missing" {
        // given:
        val request = Request()
        val response = Response()
        val interaction = RequestResponseInteraction("test interaction", emptyList(), request, response)
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionJson = ((json as Json.Object)["interactions"] as Json.Array).first() as Json.Object
        val requestJson = interactionJson["request"] as Json.Object
        val responseJson = interactionJson["response"] as Json.Object
        // then:
        interactionJson.containsKey("providerState") shouldBe false
        requestJson.containsKey("body") shouldBe false
        requestJson.containsKey("query") shouldBe false
        requestJson.containsKey("headers") shouldBe false
        requestJson.containsKey("matchingRules") shouldBe false
        requestJson.containsKey("generators") shouldBe false
        responseJson.containsKey("body") shouldBe false
        responseJson.containsKey("headers") shouldBe false
        responseJson.containsKey("generators") shouldBe false
    }

    "when writing pacts, do not parse JSON string bodies" {
        // given:
        val request = Request(body = OptionalBody.body("\"This is a string\""))
        val response = Response(body = OptionalBody.body("\"This is a string\""))
        val interaction = RequestResponseInteraction(
            "test interaction with JSON string bodies",
            emptyList(), request, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionJson = ((json as Json.Object)["interactions"] as Json.Array).first() as Json.Object
        val requestJson = interactionJson["request"] as Json.Object
        val responseJson = interactionJson["response"] as Json.Object

        // then:
        requestJson["body"].toString() shouldBe "\"This is a string\""
        responseJson["body"].toString() shouldBe "\"This is a string\""
    }

    "handle non-ascii characters correctly" {
        // given:
        val request = Request(body = OptionalBody.body("\"This is a string with letters ä, ü, ö and ß\""))
        val response = Response(body = OptionalBody.body("\"This is a string with letters ä, ü, ö and ß\""))
        val interaction = RequestResponseInteraction(
            "test interaction with non-ascii characters in bodies",
            emptyList(), request, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionJson = ((json as Json.Object)["interactions"] as Json.Array).first() as Json.Object
        val requestJson = interactionJson["request"] as Json.Object
        val responseJson = interactionJson["response"] as Json.Object

        // then:
        requestJson["body"].toString() shouldBe "\"This is a string with letters ä, ü, ö and ß\""
        responseJson["body"].toString() shouldBe "\"This is a string with letters ä, ü, ö and ß\""
    }

    // different from pact-jvm 3.6.x: Requests need to be differentiatable, not just by description
    "when writing a pact file to disk, merge the pact with any existing one" {
        // given:
        FeatureFlags.enableFeature(Feature.MERGE_EXISTING_PACTS_FILE)

        val request = Request()
        val response = Response()
        val interaction = RequestResponseInteraction(
            "test interaction",
            emptyList(), request, response
        )
        val interaction2 = RequestResponseInteraction(
            "test interaction two",
            emptyList(), request, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        pact.requestResponseInteractions = listOf(interaction2)
        val result = runCatching { PactJsonifier.generateJson(pact, pactDir) }

        // then:
        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBeException PactMergeException::class
    }

    // different from pact-jvm 3.6.x: Requests are mergable if they have any criteria to differentiate incoming requests
    "when writing a pact file to disk, merge the pact with any existin one successfully" {
        // given:
        FeatureFlags.enableFeature(Feature.MERGE_EXISTING_PACTS_FILE)

        val request = Request()
        val request2 = Request(path = "/secondary/path")
        val response = Response()
        val interaction = RequestResponseInteraction(
            "test interaction",
            emptyList(), request, response
        )
        val interaction2 = RequestResponseInteraction(
            "test interaction two",
            emptyList(), request2, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        pact.requestResponseInteractions = listOf(interaction2)
        PactJsonifier.generateJson(pact, pactDir)

        // then:
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionsJson = ((json as Json.Object)["interactions"] as Json.Array)
        interactionsJson.map {
            (it as Json.Object)["description"].toString()
        } shouldBe listOf("\"test interaction\"", "\"test interaction two\"")
    }

    "overwrite any existing pact file if the pact.writer.overwrite property is set" {
        // given:
        val request = Request()
        val response = Response()
        val interaction = RequestResponseInteraction(
            "test interaction",
            emptyList(), request, response
        )
        val interaction2 = RequestResponseInteraction(
            "test interaction two",
            emptyList(), request, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        pact.requestResponseInteractions = listOf(interaction2)
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionsJson = ((json as Json.Object)["interactions"] as Json.Array)

        // then:
        interactionsJson.map {
            (it as Json.Object)["description"].toString()
        } shouldBe listOf("\"test interaction two\"")
    }

    // pact-jvm @Issue("#877")
    "keep null attributes in the body" {
        // given:
        val request = Request(
            body = OptionalBody.body(
                "{\"settlement_summary\": {\"capture_submit_time\": null,\"captured_date\": null}}"
            )
        )
        val response = Response(
            body = OptionalBody.body(
                "{\"settlement_summary\": {\"capture_submit_time\": null,\"captured_date\": null}}"
            )
        )
        val interaction = RequestResponseInteraction(
            "test interaction with null values in bodies",
            emptyList(), request, response
        )
        val pact = RequestResponsePact(
            Provider("PactWriterSpecProvider"),
            Consumer("PactWriterSpecConsumer"), listOf(interaction)
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "pactwriterspecconsumer___pactwriterspecprovider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionJson = ((json as Json.Object)["interactions"] as Json.Array).first() as Json.Object
        val requestJson = interactionJson["request"] as Json.Object
        val responseJson = interactionJson["response"] as Json.Object
        // then:
        requestJson["body"].toString() shouldBe Json.Object("settlement_summary" to Json.Object("capture_submit_time" to Json.Null, "captured_date" to Json.Null)).toString()
        responseJson["body"].toString() shouldBe Json.Object("settlement_summary" to Json.Object("capture_submit_time" to Json.Null, "captured_date" to Json.Null)).toString()
    }

    // pact-jvm @Issue("#879")
    "when merging pact files, the original file must be read using UTF-8" {
        // given:
        val pact = RequestResponsePact(
            Provider(), Consumer(),
            listOf(
                RequestResponseInteraction(description = "Request für ping", request = Request(), response = Response(), providerStates = emptyList())
            )
        )

        // when:
        PactJsonifier.generateJson(pact, pactDir)
        PactJsonifier.generateJson(pact, pactDir)
        val pactFile = File(pactDir, "consumer___provider.json")
        val json = PactParsingUtil.parseContentToJson(pactFile.readText(Charsets.UTF_8))
        val interactionJson = ((json as Json.Object)["interactions"] as Json.Array).first() as Json.Object

        // then:
        interactionJson["description"].toString() shouldBe "\"Request für ping\""
    }
})