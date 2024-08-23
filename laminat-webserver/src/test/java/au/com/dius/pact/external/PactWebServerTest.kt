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

class PactWebServerTest {

    private lateinit var mockPactWebServer: StatefullPactWebServer

    @Before
    fun setup() {
        mockPactWebServer = StatefullPactWebServer(allowUnexpectedKeys = true, PACT_ERROR_CODE)
        mockPactWebServer.addPacts(getInitialPacts())
        mockPactWebServer.observeMatches { incomingRequest, requestMatch ->
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
    fun statefulWebServerStarted_noStateSet_returnsCorrectCounts() {
        Assert.assertEquals(1, mockPactWebServer.getDefinedPactCount())
        Assert.assertEquals(2, mockPactWebServer.getDefinedInteractionCount())
        Assert.assertEquals(0, mockPactWebServer.getCurrentInteractionCount())
        Assert.assertEquals(false, mockPactWebServer.validateInteractionsCompleted())
    }

    @Test
    fun statefulWebServerStarted_combinedStateSet_returnsCorrectCounts() {
        mockPactWebServer.setStates(listOf(STATE_1, STATE_2))

        Assert.assertEquals(1, mockPactWebServer.getDefinedPactCount())
        Assert.assertEquals(2, mockPactWebServer.getDefinedInteractionCount())
        Assert.assertEquals(2, mockPactWebServer.getCurrentInteractionCount())
        Assert.assertEquals(false, mockPactWebServer.validateInteractionsCompleted())
    }

    @Test
    fun statefulWebServerStarted_singleStateSet_returnsCorrectCounts() {
        mockPactWebServer.setStates(listOf(STATE_2))

        Assert.assertEquals(1, mockPactWebServer.getDefinedPactCount())
        Assert.assertEquals(2, mockPactWebServer.getDefinedInteractionCount())
        Assert.assertEquals(1, mockPactWebServer.getCurrentInteractionCount())
        Assert.assertEquals(false, mockPactWebServer.validateInteractionsCompleted())
    }

    @Test
    fun statefulWebServerStarted_pactsCleared_returnsCorrectCounts() {
        mockPactWebServer.clearPacts()

        Assert.assertEquals(0, mockPactWebServer.getDefinedPactCount())
        Assert.assertEquals(0, mockPactWebServer.getDefinedInteractionCount())
        Assert.assertEquals(0, mockPactWebServer.getCurrentInteractionCount())
        Assert.assertEquals(true, mockPactWebServer.validateInteractionsCompleted())
    }

    @Test
    fun statefulWebServerStarted_singleStateSet_matchesRequestCorrectly() {
        mockPactWebServer.setStates(listOf(STATE_1))

        val httpClient = createHttpClient()
        Assert.assertEquals(true, httpClient.newCall(getRequest()).execute().isSuccessful)
        Assert.assertEquals(false, httpClient.newCall(postRequest()).execute().isSuccessful)

        Assert.assertEquals(false, mockPactWebServer.validateInteractionsCompleted())
    }

    @Test
    fun statefulWebServerStarted_combinedStatesSetRequested_validatesAllPacts() {
        mockPactWebServer.setStates(listOf(STATE_1, STATE_2))

        val httpClient = createHttpClient()
        Assert.assertEquals(true, httpClient.newCall(getRequest()).execute().isSuccessful)
        Assert.assertEquals(true, httpClient.newCall(postRequest()).execute().isSuccessful)

        Assert.assertEquals(true, mockPactWebServer.validateInteractionsCompleted())
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

    companion object {
        private const val TIMEOUT = 3L

        private const val PACT_ERROR_CODE = 999
        private val STATE_1 = ProviderState("State_1")
        private val STATE_2 = ProviderState("State_2")
    }
}