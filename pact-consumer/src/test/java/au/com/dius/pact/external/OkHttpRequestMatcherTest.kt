package au.com.dius.pact.external

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.model.RequestResponseInteraction
import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
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

    private fun getMockSocket(): Socket {
        val mockInetAddress = mock(InetAddress::class.java)
        doReturn("mockhost").`when`(mockInetAddress).getHostName()

        val mockSocket = mock(Socket::class.java)
        doReturn(mockInetAddress).`when`(mockSocket).getInetAddress()
        doReturn(1234).`when`(mockSocket).getLocalPort()

        return mockSocket
    }

    @Test
    fun pactDispatcher_PostRequest_MatchesCorrectly() {
        val matcher = OkHttpRequestMatcher(false)
        val mockSocket = getMockSocket()
        val headers = Headers.Builder()
            .add("Authorization: ")
            .add("Content-Type: application/json")
            .add("Content-Length: 102")
            .add("Host: localhost:41163")
            .add("Connection: Keep-Alive")
            .add("Accept-Encoding: gzip")
            .add("User-Agent: okhttp/3.9.0")
            .add("Accept-Language: de")
            .build()
        val body = Buffer()
        body.outputStream().use {
            it.write("{ \"regex1\": \"123456789\", \"regex2\": \"abcd\"}".toByteArray())
        }
        val request = RecordedRequest("POST /test/path HTTP/1.1", headers, ArrayList(), body.size(), body,0, mockSocket)

        val interactions = testPost.interactions.map { it as RequestResponseInteraction }
        val match = matcher.findInteraction(interactions, request)

        when (match) {
            is OkHttpRequestMatcher.RequestMatch.FullRequestMatch -> return
            is OkHttpRequestMatcher.RequestMatch.PartialRequestMatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems.joinToString("\n")}")
                return
            }
            is OkHttpRequestMatcher.RequestMatch.RequestMismatch -> {
                Assert.fail("Match is only a Partial Request Match: \n${match.problems?.joinToString("\n")}")
                return
            }
        }
    }
}