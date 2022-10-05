package au.com.dius.pact.model.v3

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.StringSpecExt
import au.com.dius.pact.external.PactJsonifier
import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.PactReaderSource
import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import io.kotlintest.matchers.shouldBe
import java.io.File

class V3PactSpec: StringSpecExt() { //TODO: IMPLEMENT MESSAGE PACTING

    private lateinit var pactDirectory: File
    private lateinit var pactFile: File

    init {

        beforeTest {
            pactDirectory = File("build/pact")
            pactFile = File.createTempFile("consumer-provider", ".json", pactDirectory)
            val pactAssetFile = File(PactParsingUtil.convertToAssetFilePath("v3-message-pact.json"))
            pactFile.writeBytes(pactAssetFile.readBytes())
            FeatureFlags.enableFeature(Feature.MERGE_EXISTING_PACTS_FILE)
        }

        afterTest {
            pactFile.delete()
            FeatureFlags.restoreDefault(Feature.MERGE_EXISTING_PACTS_FILE)
        }

        "writing pacts should merge with any existing file" {
            // given:
            val pactString = Json.Object(
                "consumer" to Json.Object("name" to Json.StringPrimitive("consumer")),
                "provider" to Json.Object("name" to Json.StringPrimitive("provider")),
                "messages" to Json.Array(
                    Json.Object(
                        "providerStates" to Json.Array(Json.Object("name" to Json.StringPrimitive("a new message exists"))),
                        "contents" to Json.StringPrimitive("Hello"),
                        "description" to Json.StringPrimitive("a new hello message"),
                        "metaData" to Json.Object( "contentType" to Json.StringPrimitive("application/json"))
                    )
                ),
                "metadata" to Json.Object("pactSpecification" to Json.Object("version" to Json.StringPrimitive("3.0.0")))
            ).toString()
            val pact = PactReader.readPact(PactReaderSource.ReaderPactSource(pactString.reader()))

            // when:
            PactJsonifier.generateJson(pact, pactDirectory, PactSerializationConfig(PactSpecVersion.V3, null))
            val json = Json.parse(pactFile.readText(Charsets.UTF_8))

            // then:
            (json["messages"] as Json.Array).size shouldBe 2
            (json["messages"][0]["description"]).getValueOrNull<String>() shouldBe "a hello message"
            (json["messages"][1]["description"]).getValueOrNull<String>() shouldBe "a new hello message"
        }
    }
}