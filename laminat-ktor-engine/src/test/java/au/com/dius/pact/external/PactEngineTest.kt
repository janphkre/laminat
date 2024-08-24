package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import io.ktor.client.request.HttpResponseData
import io.ktor.http.content.OutgoingContent
import io.ktor.util.InternalAPI
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.readFully
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.CoroutineContext

@OptIn(InternalAPI::class)
class PactEngineTest: AbstractRequestTest() {

    private lateinit var pactEngine: PactEngine
    private lateinit var pactBehaviour: PactEngineBehaviour
    private lateinit var coroutineContext: CoroutineContext
    private lateinit var coroutineDispatcher: TestDispatcher

    @Before
    fun setup() {
        coroutineDispatcher = StandardTestDispatcher()
        coroutineContext = coroutineDispatcher + Job()

        pactBehaviour = StatelessPactBehaviour()
        pactEngine = PactEngine(
            coroutineContext,
            coroutineDispatcher,
            pactBehaviour,
            600
        )
    }

    @After
    fun teardown() {
        coroutineDispatcher.cancel()
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

    @Test
    fun mockPactEngine_requestsPerformedAndShutdown_ServerShutsDown() = runTest {
        val request = createGetKtorRequest()
        pactEngine.execute(request)
        pactEngine.close()
        val result = runCatching { pactEngine.execute(request) }
        Assert.assertEquals(true, result.isFailure)
        Assert.assertEquals(true, result.exceptionOrNull() != null)
    }

    @Test
    fun mockPactEngine_PostRequest_matchesCorrectly() = runTest {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val incomingRequest = createPostKtorRequestWithFullData(request)
        pactBehaviour.addPact(testPost)

        val response = pactEngine.execute(incomingRequest)
        val responseBody = getResponseBody(response)
        Assert.assertEquals("{\"regex3\":\"12345\",\"regex4\":\"abc\"}", responseBody)
        Assert.assertEquals(200, response.statusCode.value)
    }

    @Test
    fun mockPactEngine_PostRequestEmpty_NotMatching() = runTest {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val incomingRequest = createPostKtorRequest(request)

        val response = pactEngine.execute(incomingRequest)
        val responseBody = getResponseBody(response)
        Assert.assertEquals("Failed to match request at all! Best match was with null:\nnull", responseBody)
        Assert.assertEquals(600, response.statusCode.value)
    }

    private fun getResponseBody(response: HttpResponseData): String {
        return when(val content = response.body) {
            is OutgoingContent.NoContent -> {
                String()
            }
            is OutgoingContent.ByteArrayContent -> {
                String(content.bytes())
            }
            is OutgoingContent.ReadChannelContent -> {
                runBlocking {
                    val channel = content.readFrom()
                    while (!channel.isClosedForWrite) {
                        channel.awaitContent()
                    }
                    val byteArray = ByteArray(channel.availableForRead)
                    channel.readFully(byteArray)
                    String(byteArray)
                }
            }
            is OutgoingContent.WriteChannelContent -> {
                runBlocking {
                    val channel = ByteChannel()
                    content.writeTo(channel)
                    channel.flush()
                    channel.close()
                    val byteArray = ByteArray(channel.availableForRead)
                    channel.readFully(byteArray)
                    String(byteArray)
                }
            }
            is OutgoingContent.ProtocolUpgrade -> {
                String()
            }
            is String -> {
                content
            }
            is ByteArray -> {
                String(content)
            }
            else -> {
                throw IllegalArgumentException("Unhandled response body: ${content::class}")
            }
        }
    }

    @Test
    fun mockPactEngine_PostRequestUnmatched_PartialMatching() = runTest {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\"}".toByteArray()

        val incomingRequest = createPostKtorRequestWithFullData(request)
        pactBehaviour.addPact(testPost)

        val response = pactEngine.execute(incomingRequest)
        val responseBody = getResponseBody(response)
        Assert.assertTrue(
            "Expected responseBody did not start with expected string: $responseBody",
            responseBody.startsWith("Partially matched None_POST testRequest:")
        )
    }
}