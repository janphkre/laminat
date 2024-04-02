package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RecordingPactWebServerTest {

    private lateinit var mockPactWebServer: RecordingPactWebServer

    @Before
    fun setup() {
        mockPactWebServer = RecordingPactWebServer(StatelessPactWebServer(allowUnexpectedKeys = true, PACT_ERROR_CODE))
        mockPactWebServer.addPacts(getInitialPacts())
        mockPactWebServer.observeMatches { _, requestMatch ->
            println(requestMatch)
        }
    }

    @After
    fun teardown() {
        mockPactWebServer.teardown()
    }

    private fun getInitialPacts(): List<RequestResponsePact> {
        return listOf(
            ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
                .given(STATE_1.name)
                .uponReceiving("GET testRequest")
                .method("GET")
                .path("/test/path")
                .willRespondWith()
                .status(200)
                .body(PactDslJsonBody().stringType("abc", "def"))
                .toPact(),
            ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
                .given(STATE_2.name)
                .uponReceiving("POST nullTestRequest")
                .method("POST")
                .path("/test/path")
                .body(PactDslJsonRootValue.matchNull())
                .willRespondWith()
                .status(200)
                .body(PactDslJsonRootValue.matchNull())
                .toPact()
        )
    }

    @Test
    fun recordingWebServerStarted_multipleRequestsRun_validatesAllInteractions() {
        val httpClient = createHttpClient()
        Assert.assertEquals(true, httpClient.newCall(getRequest()).execute().isSuccessful)
        Assert.assertEquals(true, httpClient.newCall(postRequest()).execute().isSuccessful)

        mockPactWebServer.validateInteractions(getInitialPacts())
    }

    private fun getRequest(): Request {
        return Request.Builder()
            .get()
            .url("${mockPactWebServer.getUrlString()}test/path")
            .build()
    }

    private fun postRequest(): Request {
        return Request.Builder()
            .post("null".toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull()))
            .url("${mockPactWebServer.getUrlString()}test/path")
            .build()
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Test
    fun multipleRequestsRun_pactsAreCleared_validatesAllInteractions() {
        val httpClient = createHttpClient()
        Assert.assertEquals(true, httpClient.newCall(getRequest()).execute().isSuccessful)
        mockPactWebServer.clearPacts()
        mockPactWebServer.addPact(getInitialPacts().last())
        Assert.assertEquals(true, httpClient.newCall(postRequest()).execute().isSuccessful)

        mockPactWebServer.validateInteractions(getInitialPacts())
    }

    companion object {
        private const val TIMEOUT = 3L

        private const val PACT_ERROR_CODE = 999
        private val STATE_1 = ProviderState("State_1")
        private val STATE_2 = ProviderState("State_2")
    }
}