package au.com.dius.pact.model.v3

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.StringSpecExt
import au.com.dius.pact.external.PactJsonifier
import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.InvalidPactException
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.PactReaderSource
import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.shouldBeException
import io.kotlintest.matchers.shouldBe
import java.io.File

class V3PactSpec : StringSpecExt() {

    private lateinit var pactDirectory: File
    private lateinit var pactFile: File

    init {

        beforeTest {
            pactDirectory = File("build/pacts")
            pactDirectory.deleteRecursively()
            pactDirectory.mkdirs()
            pactFile = File(pactDirectory, "test_consumer___test_provider.json")
            val pactAssetFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_v3.json"))
            pactFile.writeBytes(pactAssetFile.readBytes())
            FeatureFlags.enableFeature(Feature.MERGE_EXISTING_PACTS_FILE)
        }

        afterTest {
            pactDirectory.deleteRecursively()
            FeatureFlags.restoreDefault(Feature.MERGE_EXISTING_PACTS_FILE)
            FeatureFlags.restoreDefault(Feature.MERGE_DISALLOW_DIFFERENT_PACT_VERSIONS)
        }

        "writing pacts should merge with any existing file" {
            // given:
            val pactString = Json.Object(
                "consumer" to Json.Object("name" to Json.StringPrimitive("test_consumer")),
                "provider" to Json.Object("name" to Json.StringPrimitive("test_provider")),
                "interactions" to Json.Array(
                    Json.Object(
                        "providerStates" to Json.Array(Json.Object("name" to Json.StringPrimitive("a new request exists"))),
                        "request" to Json.Object(),
                        "response" to Json.Object(),
                        "description" to Json.StringPrimitive("a new hello request")
                    )
                ),
                "metadata" to Json.Object("pactSpecification" to Json.Object("version" to Json.StringPrimitive("3.0.0")))
            ).toString()
            val pact = PactReader.readPact(PactReaderSource.ReaderPactSource(pactString.reader()))

            // when:
            PactJsonifier.generateJson(pact, pactDirectory, PactSerializationConfig(PactSpecVersion.V3, null))
            val json = Json.parse(pactFile.readText(Charsets.UTF_8))

            println("Result:\n$json")

            // then:
            (json["interactions"] as Json.Array).size shouldBe 2
            (json["interactions"][1]["description"]).getValueOrNull<String>() shouldBe "test interaction"
            (json["interactions"][0]["description"]).getValueOrNull<String>() shouldBe "a new hello request"
        }

        // different from pact-jvm: Message-Pacts are not implemented, Merging is tested in PactWriterSpec

        "refuse to merge pacts with different spec versions" {
            // given:
            FeatureFlags.enableFeature(Feature.MERGE_DISALLOW_DIFFERENT_PACT_VERSIONS)
            val pactAssetFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_v2.json"))
            pactFile.writeBytes(pactAssetFile.readBytes())

            val pactString = Json.Object(
                "consumer" to Json.Object("name" to Json.StringPrimitive("test_consumer")),
                "provider" to Json.Object("name" to Json.StringPrimitive("test_provider")),
                "interactions" to Json.Array(
                    Json.Object(
                        "providerStates" to Json.Array(Json.Object("name" to Json.StringPrimitive("a new request exists"))),
                        "request" to Json.Object(),
                        "response" to Json.Object(),
                        "description" to Json.StringPrimitive("a new hello request")
                    )
                ),
                "metadata" to Json.Object("pactSpecification" to Json.Object("version" to Json.StringPrimitive("3.0.0")))
            ).toString()
            val pact = PactReader.readPact(PactReaderSource.ReaderPactSource(pactString.reader()))

            // when:
            val result = runCatching { PactJsonifier.generateJson(pact, pactDirectory, PactSerializationConfig(PactSpecVersion.V3, null)) }

            // then:
            val exception = result.exceptionOrNull()
            exception shouldBeException (InvalidPactException::class)
            exception!!.message shouldBe "Cannot merge pacts as they are not compatible:\nFile already contains pact in version 2.0.0, but serialization config with version 3.0.0 was defined"
        }

        "merge pacts with different spec versions" {
            // given:
            val pactAssetFile = File(PactParsingUtil.convertToAssetFilePath("test_pact_v2.json"))
            pactFile.writeBytes(pactAssetFile.readBytes())

            val pactString = Json.Object(
                "consumer" to Json.Object("name" to Json.StringPrimitive("test_consumer")),
                "provider" to Json.Object("name" to Json.StringPrimitive("test_provider")),
                "interactions" to Json.Array(
                    Json.Object(
                        "providerStates" to Json.Array(Json.Object("name" to Json.StringPrimitive("a new request exists"))),
                        "request" to Json.Object(),
                        "response" to Json.Object(),
                        "description" to Json.StringPrimitive("a new hello request")
                    )
                ),
                "metadata" to Json.Object("pactSpecification" to Json.Object("version" to Json.StringPrimitive("3.0.0")))
            ).toString()
            val pact = PactReader.readPact(PactReaderSource.ReaderPactSource(pactString.reader()))

            // when:
            val result = runCatching { PactJsonifier.generateJson(pact, pactDirectory, PactSerializationConfig(PactSpecVersion.V3, null)) }

            // then:
            val exception = result.exceptionOrNull()
            exception shouldBe null
        }
    }
}