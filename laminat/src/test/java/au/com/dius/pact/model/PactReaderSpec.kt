package au.com.dius.pact.model

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.StringSpecExt
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.serialization.RequestResponsePactV2Deserializer
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import com.google.gson.JsonSyntaxException
import io.kotlintest.matchers.match
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldHave
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.mockito.Mockito
import org.mockito.Mockito.only
import java.io.File
import java.io.StringReader

class PactReaderSpec : StringSpecExt({

    beforeTest {
        mockkConstructor(RequestResponsePactV3Deserializer::class)
        every { anyConstructed<RequestResponsePactV3Deserializer>().isValid(any()) } answers { callOriginal() }
        every { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) } answers { callOriginal() }
        mockkConstructor(RequestResponsePactV2Deserializer::class)
        every { anyConstructed<RequestResponsePactV2Deserializer>().isValid(any()) } answers { callOriginal() }
        every { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) } answers { callOriginal() }
    }

    afterTest {
        unmockkAll()
    }

    "loads a pact with no metadata as V2" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("pact.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        // then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    "loads a pact with V1 version using existing loader" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v1-pact.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        // then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    "loads a pact with V2 version using existing loader" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v2-pact.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    "loads a pact with V3 version using V3 loader" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v3-pact.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        verify(exactly = 0) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) }
        verify(exactly = 1) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    "loads a pact with old version format" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v3-pact-old-format.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        verify(exactly = 0) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) }
        verify(exactly = 1) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    // TODO! MESSAGE PACTS
    /*"loads a message pact with V3 version using V3 loader" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v3-pact-old-format.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        verify(exactly = 0) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) }
        verify(exactly = 1) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        pact should { it is MessagePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }*/

    "loads a pact from an inputstream" {
        // given:
        val pactFileInputStream = File(PactParsingUtil.convertToAssetFilePath("pact.json")).inputStream()

        // when:
        val pact = PactReader.readPact(PactReaderSource.InputStreamPactSource(pactFileInputStream))

        //then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.InputStreamPactSource }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.InputStreamPactSource
    }

    "loads a pact from a json string" {
        // given:
        val pactFileText = File(PactParsingUtil.convertToAssetFilePath("pact.json")).readText(Charsets.UTF_8)

        // when:
        val pact = PactReader.readPact(PactReaderSource.ReaderPactSource(StringReader(pactFileText)))

        //then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.ReaderPactSource }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.ReaderPactSource
    }

    "throws an exception if it can not load the pact file" {
        // given:
        val pactString = "this is not a pact file!"

        // when:
        val pactResult = runCatching { PactReader.readPact(PactReaderSource.ReaderPactSource(StringReader(pactString))) }

        // then:
        pactResult.exceptionOrNull() should { it is JsonSyntaxException }

        verify(exactly = 0) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
    }

    "handles invalid version metadata" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("pact-invalid-version.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    // different from pact-jvm 3.6.x: Does not support a http client currently

    "correctly loads V2 pact query strings" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v2_pact_query.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        pact.requestResponseInteractions[0].request.query shouldBe mapOf("q" to listOf("p", "p2"), "r" to  listOf("s"))
        pact.requestResponseInteractions[1].request.query shouldBe mapOf("datetime" to listOf("2011-12-03T10:15:30+01:00"), "description" to listOf("hello world!"))
        pact.requestResponseInteractions[2].request.query shouldBe mapOf("options" to listOf("delete.topic.enable=true"), "broker" to listOf("1"))
    }

    "Defaults to V3 pact provider states" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_v3.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        pact.requestResponseInteractions[0].providerStates shouldBe listOf(ProviderState("test state", mapOf("name" to "Testy")), ProviderState("test state 2", mapOf("name" to "Testy2")))
    }

    "Falls back to the to V2 pact provider state" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_v3_old_provider_state.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        pact.requestResponseInteractions[0].providerStates shouldBe listOf(ProviderState("test state"))
    }

    "reads from classpath inside jar" {
        // given:
        val pactPath = "classpath:jar-pacts/test_pact_v3.json" //TODO!

        // when:
        val pact = PactReader.readPact(PactReaderSource.ClassPathPactSource(pactPath))

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        pact.requestResponseInteractions[0].providerStates shouldBe listOf(ProviderState("test state", mapOf("name" to "Testy")), ProviderState("test state 2", mapOf("name" to "Testy2")))
    }

    "throws a meaningful exception when reading from non-existent classpath" {
        // given:
        val pactPath = "classpath:no_such_pact.json" //TODO!

        // when:
        val pactResult = runCatching { PactReader.readPact(PactReaderSource.ClassPathPactSource(pactPath)) }

        // then:
        pactResult.exceptionOrNull() should { it is RuntimeException && it.message!!.contains("no_such_pact.json") }

        verify(exactly = 0) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(any(), any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
    }

    "correctly loads V2 pact with string bodies" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_with_string_body.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.FileSource(pactFile))

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        (pact.requestResponseInteractions[0].request.body as OptionalBody.StringBody).unwrap() shouldBe "\"This is a string\""
        (pact.requestResponseInteractions[0].response.body as OptionalBody.StringBody).unwrap() shouldBe "\"This is a string\""
    }

    "loads a pact where the source is a closure" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("pact.json"))

        // when:
        val pact = PactReader.readPact(PactReaderSource.ClosurePactSource { PactReaderSource.FileSource(pactFile) })

        //then:
        verify(exactly = 1) { anyConstructed<RequestResponsePactV2Deserializer>().createPact(match { it == PactSource.FileSource(pactFile) }, any()) }
        verify(exactly = 0) { anyConstructed<RequestResponsePactV3Deserializer>().createPact(any(), any()) }
        pact should { it is RequestResponsePact }
        pact.source shouldBe PactSource.FileSource(pactFile)
    }

    "when loading a pact with V2 version from the broker, it preserves the interaction ids" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v2-pact-broker.json"))
        val regex = Regex("^[a-zA-Z0-9]+$")
        // when:
        val pact = PactReader.readPact(PactReaderSource.ClosurePactSource { PactReaderSource.FileSource(pactFile) })

        //then:
        pact should { it is RequestResponsePact }
        pact.interactions.forEach { interaction ->
            //interaction should { regex.matches(it.interactionId) } // TODO: interaction id does not exist
        }
    }

    "when loading a pact with V3 version from the broker, it preserves the interaction ids" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v3-pact-broker.json"))
        val regex = Regex("^[a-zA-Z0-9]+$")
        // when:
        val pact = PactReader.readPact(PactReaderSource.ClosurePactSource { PactReaderSource.FileSource(pactFile) })

        //then:
        pact should { it is RequestResponsePact }
        pact.interactions.forEach { interaction ->
            //interaction should { regex.matches(it.interactionId) } // TODO: interaction id does not exist
        }
    }

    // TODO! MESSAGE PACTS
    /*"when loading a message pact from the broker, it preserves the interaction ids" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("v3-pact-broker.json"))
        val regex = Regex("^[a-zA-Z0-9]+$")
        // when:
        val pact = PactReader.readPact(PactReaderSource.ClosurePactSource { PactReaderSource.FileSource(pactFile) })

        //then:
        pact should { it is MessagePact }
        pact.interactions.forEach { interaction ->
            interaction should { regex.matches(it.interactionId) }
        }
    }*/

    "determining pact spec version" {
        forAll(
            table(
        
                headers("json", "version"),
                row("{}", PactSpecVersion.V2),
        row("{\"metadata\":{}}", PactSpecVersion.V2),
                row("{\"metadata\":{\"pactSpecificationVersion\":\"1.2.3\"}}"       , PactSpecVersion.V2),
            row("{\"metadata\":{\"pactSpecification\":\"1.2.3\"}}"              , PactSpecVersion.V2),
        row("{\"metadata\":{\"pactSpecification\":{}}}"                   , PactSpecVersion.V2),
        row("{\"metadata\":{\"pactSpecification\":{\"version\":\"1.2.3\"}}}"  , PactSpecVersion.V2),
        row("{\"metadata\":{\"pactSpecification\":{\"version\":\"3.0\"}}}"    , PactSpecVersion.V3),
        row("{\"metadata\":{\"pact-specification\":{\"version\":\"1.2.3\"}}}" , PactSpecVersion.V2),
        )
        ) { json, version ->
            val parsedJson = Json.parse(json)
            PactReader.determineSpecVersion(parsedJson) shouldBe version
        }
    }

    // pact-jvm @Issue('#1031')
    "handle encoded values in the pact file" {
        // given:
        val pactFile = File(PactParsingUtil.convertToAssetFilePath("encoded-values-pact.json"))
        val regex = Regex("^[a-zA-Z0-9]+$")
        // when:
        val pact = PactReader.readPact(PactReaderSource.ClosurePactSource { PactReaderSource.FileSource(pactFile) })

        //then:
        pact should { it is RequestResponsePact }
        pact as RequestResponsePact
        (pact.requestResponseInteractions[0].request.body as OptionalBody.StringBody).unwrap() shouldBe "{\"entityName\":\"mock-name\",\"xml\":\"<?xml version=\\\\\"1.0\\\\\" encoding=\\\\\"UTF-8\\\\\"?>\\\\n\"}"
        (pact.requestResponseInteractions[0].response.body as OptionalBody.StringBody).unwrap() shouldBe "{\\n  \"entityName\": \"\${eName}\",\\n  \"xml\": \"<?xml version=\\\\\"1.0\\\\\" encoding=\\\\\"UTF-8\\\\\"?>\\\\n\"\\n}"
}
})