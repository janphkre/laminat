package au.com.dius.pact.external

import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.external.dsl.array
import au.com.dius.pact.external.dsl.duplicate
import au.com.dius.pact.external.dsl.getAllPacts
import au.com.dius.pact.external.dsl.obj
import au.com.dius.pact.external.dsl.pact
import au.com.dius.pact.external.dsl.request
import au.com.dius.pact.external.dsl.response
import au.com.dius.pact.external.dsl.stringMatcher
import au.com.dius.pact.external.dsl.stringType
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.readFile
import org.junit.Assert
import org.junit.Test
import java.io.File

class ExternalDslTest {

    object TestRequests {

        val initialRequest get() = request {
            uponReceiving("GET testRequest")
            .method("GET")
            .path("test/path")
            .headers(defaultRequestHeaders)
        }

        private val defaultRequestHeaders = hashMapOf(
            Pair("We", "will have to see about this!")
        )

        private fun request(initializer: PactDslWithProvider.() -> PactDslRequestWithPath): PactDslRequestWithPath {
            return request("testconsumer", "testproducer", initializer)
        }
    }

    object TestResponses {

        private val nullableErrorType: String? = "FATAL"
        private val nullableExampleString: String? = "NullableExampleString"

        val initialResponse by response { this }

        val errorResponse by response {
            stringMatcher("errorType", ".*", nullableErrorType)
            .array("messages") {
                obj {
                    decimalType("opacity",0.9)
                    stringType("message","Error messsage.")
                }
                .obj {
                    decimalType("opacity",0.3)
                    stringType("message","Info messsage.")
                }
            }
            .obj("exampleObj") {
                stringType("exampleString", nullableExampleString)
            }
        }
    }

    object TestPacts {

        val initialPact by pact {
            TestRequests.initialRequest
                .willRespondWith()
                .status(200)
                .headers(defaultResponseHeaders)
                .body(TestResponses.initialResponse)
        }

        val errorPact by duplicate(ProviderState("ERROR"), { initialPact }) {
            status(500)
                .headers(defaultResponseHeaders)
                .body(TestResponses.errorResponse)
        }

        private val defaultResponseHeaders = hashMapOf(
            Pair("We", "will have to see about this as well.")
        )
    }

    private val expectedPact = "testconsumer:testproducer.json"

    @Test
    fun externalPact_buildJson_correctlyBuilt() {
        PactJsonifier.generateJson(listOf(TestPacts.initialPact), File("pacts"))
        val outputPactFile = File("pacts/$expectedPact")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = readFile(outputPactFile)
        val expectedPact = readFile(File("src/test/assets/$expectedPact"))
        Assert.assertEquals("Generated pact does not match expectations!", expectedPact, outputPact)
    }

    @Test
    fun externalPact_collectAllJson_AllItemsReturned() {
        val pactList = TestPacts.getAllPacts()
        TODO()
    }
}