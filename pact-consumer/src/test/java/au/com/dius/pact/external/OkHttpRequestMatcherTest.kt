package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.RequestResponseInteraction
import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import java.io.File
import java.net.InetAddress
import java.net.Socket

class OkHttpRequestMatcherTest {

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
                    .minArrayLike("nestedArray",2)
                    .stringMatcher("regex5","\\d{9}")
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
                                .minArrayLike("nestedArray",80)
                                .stringMatcher("uid","[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|([A-Z0-9]{40})")
                                .closeObject()
                                !!.closeArray()
                            .closeObject()
                        !!.closeArray()
                    .closeObject()
            )
            .toPact()
    }

    private fun getMockSocket(): Socket {
        val mockInetAddress = mock(InetAddress::class.java)
        doReturn("mockhost").`when`(mockInetAddress).getHostName()

        val mockSocket = mock(Socket::class.java)
        doReturn(mockInetAddress).`when`(mockSocket).getInetAddress()
        doReturn(1234).`when`(mockSocket).getLocalPort()

        return mockSocket
    }

    private fun getRecordedRequest(requestBody: ByteArray): RecordedRequest {
        val mockSocket = getMockSocket()
        val headers = Headers.Builder()
            .add("Authorization: ")
            .add("Content-Type: application/json")
            .add("Content-Length: ${requestBody.size}")
            .add("Host: localhost:41163")
            .add("Connection: Keep-Alive")
            .add("Accept-Encoding: gzip")
            .add("User-Agent: okhttp/3.9.0")
            .add("Accept-Language: de")
            .build()
        val body = Buffer()
        body.outputStream().use {
            it.write(requestBody)
        }
        return RecordedRequest("POST /test/path HTTP/1.1", headers, ArrayList(), body.size(), body,0, mockSocket)
    }

    @Test
    fun pactDispatcher_PostRequest_MatchingCorrectly() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val matcher = OkHttpRequestMatcher(false)
        val recordedRequest = getRecordedRequest(request)

        val interactions = testPost.interactions.map { it as RequestResponseInteraction }
        val match = matcher.findInteraction(interactions, recordedRequest)

        when (match) {
            is OkHttpRequestMatcher.RequestMatch.FullRequestMatch -> return
            is OkHttpRequestMatcher.RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is OkHttpRequestMatcher.RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactDispatcher_PostRequest_NotMatching() {
        val request = "{ \"regex1\": \"1\", \"decimal1\": 50.999234, \"unexpected\":\"skdfskjdf\"}".toByteArray()

        val matcher = OkHttpRequestMatcher(false)
        val recordedRequest = getRecordedRequest(request)

        val interactions = testPost.interactions.map { it as RequestResponseInteraction }
        val match = matcher.findInteraction(interactions, recordedRequest)

        when (match) {
            is OkHttpRequestMatcher.RequestMatch.FullRequestMatch -> {
                Assert.fail("Match was a Full Request Match!")
            }
            is OkHttpRequestMatcher.RequestMatch.PartialRequestMatch -> {
                Assert.assertEquals("MismatchedBody on null:\nExpected '1' to match '\\d{8,9}'", match.problems[0].message)
                Assert.assertEquals("MismatchedBody on \$:\nExpected regex2=\"abcd\" but was missing", match.problems[1].message)
            }
            is OkHttpRequestMatcher.RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactDispatcher_PostArrayRequest_MatchingCorrectly() {
        val arrayObject = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}"
        val nestedArrayObject = "{ \"nestedArray\": [ { \"regex5\": \"123456789\" }, { \"regex5\": \"987654321\" } ] }"
        val request = "{ \"array\": [$arrayObject,$arrayObject,$nestedArrayObject]}".toByteArray()

        val matcher = OkHttpRequestMatcher(false)
        val recordedRequest = getRecordedRequest(request)

        val interactions = testPostArray.interactions.map { it as RequestResponseInteraction }
        val match = matcher.findInteraction(interactions, recordedRequest)

        when (match) {
            is OkHttpRequestMatcher.RequestMatch.FullRequestMatch -> return
            is OkHttpRequestMatcher.RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
            }
            is OkHttpRequestMatcher.RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Request Mismatch: \n${match.problems?.joinToString("\n")}")
            }
        }
    }

    @Test
    fun pactDispatcher_Serialize_Pact() {
        PactJsonifier.generateJson(listOf(testPostArray), File("build/outputs/pact"))
    }
}