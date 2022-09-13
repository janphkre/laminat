package au.com.dius.pact

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.external.PactJsonifier
import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.RequestResponsePact
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import org.junit.Assert
import org.junit.Test

/**
 * this is a larger test that checks the pact dsl and its json generation
 * against a fixed pact json in the assets.
 *
 * @author Jan Phillip Kretzschmar
 */
class PactTest {

    private val expectedPact = "testconsumer___testproducer.json"
    private val expectedBinaryPact = "testbinaryconsumer___testproducer.json"

    private val defaultRequestHeaders = hashMapOf(
        Pair("We", "will have to see about this!")
    )
    private val defaultResponseHeaders = hashMapOf(
        Pair("We", "will have to see about this as well.")
    )

    private fun getInitialPacts(): List<RequestResponsePact> {
        return listOf(
            ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
                .uponReceiving("GET testRequest")
                .method("GET")
                .path("test/path")
                .headers(defaultRequestHeaders)
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body(PactDslJsonBody().stringType("abc", "def"))
                .uponReceiving("POST nullTestRequest")
                .method("POST")
                .path("test/path")
                .headers(defaultRequestHeaders)
                .body(PactDslJsonRootValue.matchNull())
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body(PactDslJsonRootValue.matchNull())
                .toPact()
        )
    }

    private fun getAdditionalPacts(): List<RequestResponsePact> {
        return listOf(
            ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
                .uponReceiving("GET additionaltestRequest")
                .method("GET")
                .path("test/path/additional")
                .headers(defaultRequestHeaders)
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body("{}")
                .toPact()
        )
    }

    private fun getConflictPacts(): List<RequestResponsePact> {
        return listOf(
            ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
                .uponReceiving("GET testRequest")
                .method("GET")
                .path("test/path")
                .headers(defaultRequestHeaders)
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body("{}")
                .toPact()
        )
    }

    private fun getBinaryPacts(): List<RequestResponsePact> {
        return listOf(
            ConsumerPactBuilder("TestBinaryConsumer").hasPactWith("TestProducer")
                .uponReceiving("POST testRequest")
                .method("POST")
                .path("test/path")
                .headers(defaultRequestHeaders)
                .body(ByteArray(15) { it.toByte() })
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body(ByteArray(128) { it.toByte() })
                .toPact()
        )
    }

    @Test
    fun pact_buildJson_correctlyBuilt() {
        PactJsonifier.generateJson(getInitialPacts(), File("pacts"))
        val outputPactFile = File("pacts/$expectedPact")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText()
        val expectedPactJson = File("src/test/assets/$expectedPact").readText()
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        val expectedPactTree = gson.fromJson<JsonObject>(expectedPactJson, JsonObject::class.java)
        expectedPactTree.getAsJsonObject("metadata").getAsJsonObject("pact-laminat-android").addProperty("version", BuildConfig.VERSION_NAME)
        val expectedPactString = gson.toJson(expectedPactTree)

        Assert.assertEquals("Generated pact does not match expectations!", expectedPactString, outputPact)
    }

    private inline fun <reified T> Map<*, *>.checkedField(key: String): T {
        val entry = this[key]
        Assert.assertNotNull("Could not find field for key $key!", entry)
        Assert.assertTrue("Field at $key is not of expected type!", entry is T)
        return entry as T
    }

    @Test(expected = PactMergeException::class)
    fun pact_buildJson_failsMerge() {
        PactJsonifier.generateJson(getInitialPacts().plus(getConflictPacts()), File("pacts"))
    }

    @Test
    fun pact_pactsList_noConflicts() {
        val pacts = getInitialPacts().plus(getAdditionalPacts())
        for (first in pacts) {
            for (second in pacts) {
                if (first === second) {
                    continue
                }
                Assert.assertTrue("Pacts are not compatible: $first; $second", first.compatibleTo(second))
                val conflicts = first.conflictsWith(second)
                Assert.assertTrue(
                    "Pacts are incompatible as there were ${conflicts.size} conflict(s) " +
                        "between the interactions - ${conflicts.joinToString("\n")}",
                    conflicts.isEmpty()
                )
            }
        }
    }

    @Test
    fun pact_conflictingPactList_showConflicts() {
        val pacts = getInitialPacts().plus(getConflictPacts())
        for (first in pacts) {
            for (second in pacts) {
                if (first === second) {
                    continue
                }
                Assert.assertTrue("Pacts are not compatible: $first; $second", first.compatibleTo(second))
                val conflicts = first.conflictsWith(second)
                Assert.assertEquals("Expected 1 conflict, found ${conflicts.size}!", 1, conflicts.size)
            }
        }
    }

    @Test
    fun pact_buildBinary_correctlyBuilt() {
        PactJsonifier.generateJson(getBinaryPacts(), File("pacts"))
        val outputPactFile = File("pacts/$expectedBinaryPact")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText()
        val expectedPactJson = File("src/test/assets/$expectedBinaryPact").readText()
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        val expectedPactTree = gson.fromJson<JsonObject>(expectedPactJson, JsonObject::class.java)
        expectedPactTree.getAsJsonObject("metadata").getAsJsonObject("pact-laminat-android").addProperty("version", BuildConfig.VERSION_NAME)
        val expectedPactString = gson.toJson(expectedPactTree)

        Assert.assertEquals("Generated pact does not match expectations!", expectedPactString, outputPact)
    }

    @Test
    fun pact_buildBinary_hasCorrectResponse() {
        val responseBody = getBinaryPacts().first().requestResponseInteractions.first().response.generateResponse().body.asBinary(Charsets.UTF_8)

        Assert.assertArrayEquals("Generated pact does not match expectations!", ByteArray(128) { it.toByte() }, responseBody)
    }
}