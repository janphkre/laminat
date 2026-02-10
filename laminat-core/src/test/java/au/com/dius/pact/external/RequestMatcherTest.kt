package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.RequestResponseInteraction
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.junit.Assert
import org.junit.Test
import java.nio.charset.Charset

/**
 * This test checks that the RequestMatcher matches incoming requests correctly.
 *
 * @see RequestMatcher
 * @author Jan Phillip Kretzschmar
 */
class RequestMatcherTest {

    private fun getIncomingRequest(
        requestBody: ByteArray,
        method: String = "POST",
        authorization: String = "",
        contentType: String = "application/json"
    ): IncomingRequest {
        return mockk {
            every { getMethod() } returns method
            every { getEncodedPath() } returns "/test/path"
            every { getBody() } returns requestBody
            every { getBodySize() } returns requestBody.size.toLong()
            every { getCompleteHeaders() } returns mapOf(
                "Authorization" to listOf(authorization),
                "Content-Type" to listOf(contentType),
                "Content-Length" to listOf(requestBody.size.toString()),
                "Host" to listOf("localhost:41163"),
                "Connection" to listOf("Keep-Alive"),
                "Accept-Encoding" to listOf("gzip"),
                "User-Agent" to listOf("test/request/matcher"),
                "Accept-Language" to listOf("de"),
            )
            every { queryParameterNames() } returns emptySet()
            every { queryParameterValues(any()) } returns emptyList()
            every { getCookie() } returns emptyList()
            every { getBodyAsString() } answers { String(requestBody) }
            every { getContentType() } returns contentType
            every { getBodyCharset() } returns Charset.forName("UTF-8")
        }
    }

    private val unusedPOST by lazy {
        ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("POST unusedRequest")
            .method("POST")
            .path("/unused/path")
            .headers(
                hashMapOf(
                    Pair("Content-Type", "application/json")
                )
            )
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex1", "\\d{8,9}", "123456789")
                    .stringMatcher("regex2", ".{4}", "abcd")
                    .decimalType("decimal1", 50.99234)
            )
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/json; charset=UTF-8")))
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex3", "\\d{5,6}", "12345")
                    .stringMatcher("regex4", ".{3}", "abc")
            )
            .toPact()
    }

    private val testPost by lazy {
        ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("POST testRequest")
            .method("POST")
            .path("/test/path")
            .headers(
                hashMapOf(
                    Pair("Content-Type", "application/json")
                )
            )
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex1", "\\d{8,9}", "123456789")
                    .stringMatcher("regex2", ".{4}", "abcd")
                    .decimalType("decimal1", 50.99234)
            )
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/json; charset=UTF-8")))
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex3", "\\d{5,6}", "12345")
                    .stringMatcher("regex4", ".{3}", "abc")
            )
            .toPact()
    }

    private val testPostArray by lazy {
        ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("POST testRequest")
            .method("POST")
            .path("/test/path")
            .headers(
                hashMapOf(
                    Pair("Content-Type", "application/json")
                )
            )
            .body(
                PactDslJsonBody()
                    .array("array")
                    .`object`()
                    .stringMatcher("regex1", "\\d{8,9}", "123456789")
                    .stringMatcher("regex2", ".{4}", "abcd")
                    .decimalType("decimal1", 50.99234)
                    .closeObject()
                !!.`object`()
                    .stringMatcher("regex1", "\\d{8,9}", "123456789")
                    .stringMatcher("regex2", ".{4}", "abcd")
                    .decimalType("decimal1", 50.99234)
                    .closeObject()
                !!.`object`()
                    .minArrayLike("nestedArray", 2)
                    .stringMatcher("regex5", "\\d{9}")
                    .closeObject()
                !!.closeArray()
                    .closeObject()
                !!.closeArray()

            )
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/json; charset=UTF-8")))
            .body(
                PactDslJsonBody()
                    .`object`("_embedded")
                    .stringMatcher("regex3", "\\d{5,6}", "12345")
                    .stringMatcher("regex4", ".{3}", "abc")
                    .array("firstArray")
                    .`object`().closeObject()
                !!.`object`().closeObject()
                !!.`object`().closeObject()
                !!.`object`()
                    .minArrayLike("nestedArray", 80)
                    .stringMatcher("uid", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|([A-Z0-9]{40})")
                    .closeObject()
                !!.closeArray()
                    .closeObject()
                !!.closeArray()
                    .closeObject()
            )
            .toPact()
    }

    private val hugeAuthorization = String(CharArray(200000) { 'C' })

    private val testGet by lazy {
        ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("GET huge authorization header")
            .method("GET")
            .path("/test/path")
            .headers(
                hashMapOf(
                    Pair("Authorization", hugeAuthorization)
                )
            )
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/json; charset=UTF-8")))
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex3", "\\d{5,6}", "12345")
                    .stringMatcher("regex4", ".{3}", "abc")
            )
            .toPact()
    }

    private val testPostBinary by lazy {
        ConsumerPactBuilder("TestBinaryConsumer").hasPactWith("TestProducer")
            .uponReceiving("POST testRequest")
            .method("POST")
            .path("/test/path")
            .headers(
                hashMapOf(
                    Pair("Content-Type", "application/octet-stream")
                )
            )
            .body(ByteArray(15) { it.toByte() })
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/octet-stream")))
            .body(ByteArray(128) { it.toByte() })
            .toPact()
    }

    @Test
    fun pactMatcher_PostRequest_MatchingCorrectly() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val matcher = RequestMatcher(false)
        val incomingRequest = getIncomingRequest(request)

        val interactions = testPost.interactions.map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, incomingRequest)) {
            is RequestMatch.FullRequestMatch -> return
            is RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactMatcher_PostRequestMultipleInteractions_MatchingCorrectly() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val matcher = RequestMatcher(false)
        val recordedRequest = getIncomingRequest(request)

        val interactions = testPost.interactions.plus(unusedPOST.interactions).map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, recordedRequest)) {
            is RequestMatch.FullRequestMatch -> return
            is RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactMatcher_PostRequest_NotMatching() {
        val request = "{ \"regex1\": \"1\", \"decimal1\": 50.999234, \"unexpected\":\"skdfskjdf\"}".toByteArray()

        val matcher = RequestMatcher(false)
        val recordedRequest = getIncomingRequest(request)

        val interactions = testPost.interactions.map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, recordedRequest)) {
            is RequestMatch.FullRequestMatch -> {
                Assert.fail("Match was a Full Request Match!")
            }
            is RequestMatch.PartialRequestMatch -> {
                Assert.assertEquals("MismatchedBody on null:\nExpected '1' to match '\\d{8,9}'", match.problems[0].message)
                Assert.assertEquals("MismatchedBody on \$:\nExpected regex2=\"abcd\" but was missing", match.problems[1].message)
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactMatcher_PostArrayRequest_MatchingCorrectly() {
        val arrayObject = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}"
        val nestedArrayObject = "{ \"nestedArray\": [ { \"regex5\": \"123456789\" }, { \"regex5\": \"987654321\" } ] }"
        val request = "{ \"array\": [$arrayObject,$arrayObject,$nestedArrayObject]}".toByteArray()

        val matcher = RequestMatcher(false)
        val recordedRequest = getIncomingRequest(request)

        val interactions = testPostArray.interactions.map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, recordedRequest)) {
            is RequestMatch.FullRequestMatch -> {
                val response = match.interaction.response.generateResponse()
                Assert.assertNotNull(response)
            }
            is RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactMatcher_LongGetRequest_MatchingCorrectly() {
        val matcher = RequestMatcher(false)

        val recordedRequest = getIncomingRequest(ByteArray(0), "GET", hugeAuthorization)

        val interactions = testGet.interactions.map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, recordedRequest)) {
            is RequestMatch.FullRequestMatch -> return
            is RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactMatcher_Serialize_Pact() {
        val rootFile = File("build/outputs/pact")
        rootFile.mkdirs()
        PactJsonifier.generateJson(listOf(testPostArray), rootFile)
    }

    @Test
    fun pactMatcher_PostBinaryRequest_MatchingCorrectly() {
        val matcher = RequestMatcher(false)

        val recordedRequest = getIncomingRequest(
            ByteArray(15) { it.toByte() },
            contentType = "application/octet-stream"
        )

        val interactions = testPostBinary.interactions.map { it as RequestResponseInteraction }

        when (val match = matcher.findInteraction(interactions, recordedRequest)) {
            is RequestMatch.FullRequestMatch -> return
            is RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }
}