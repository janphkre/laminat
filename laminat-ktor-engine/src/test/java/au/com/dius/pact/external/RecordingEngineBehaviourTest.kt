package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RecordingEngineBehaviourTest: AbstractRequestTest() {

    private lateinit var mockEngineBehaviour: RecordingEngineBehaviour

    @Before
    fun setup() {
        mockEngineBehaviour = RecordingEngineBehaviour(StatelessPactBehaviour())
        mockEngineBehaviour.addPacts(getInitialPacts())
        mockEngineBehaviour.observeMatches { _, requestMatch ->
            println(requestMatch)
        }
    }

    @After
    fun teardown() {
        mockEngineBehaviour.teardown()
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
        Assert.assertTrue(mockEngineBehaviour.createMatch(getRequest()) is RequestMatch.FullRequestMatch)
        Assert.assertTrue(mockEngineBehaviour.createMatch(postRequest()) is RequestMatch.FullRequestMatch)

        mockEngineBehaviour.validateInteractions(getInitialPacts())

    }

    private fun getRequest(): IncomingRequest {
        return createIncomingRequest(
            method = "GET",
            path = "mockExampleUrlBaseString/test/path"
        )
    }

    private fun postRequest(): IncomingRequest {
        return createIncomingRequest(
            requestBody = "null".toByteArray(),
            method = "POST",
            contentType = "application/json; charset=UTF-8",
            path = "mockExampleUrlBaseString/test/path"
        )
    }

    @Test
    fun multipleRequestsRun_pactsAreCleared_validatesAllInteractions() {
        Assert.assertTrue(mockEngineBehaviour.createMatch(getRequest()) is RequestMatch.FullRequestMatch)
        mockEngineBehaviour.clearPacts()
        mockEngineBehaviour.addPact(getInitialPacts().last())
        Assert.assertTrue(mockEngineBehaviour.createMatch(postRequest()) is RequestMatch.FullRequestMatch)

        mockEngineBehaviour.validateInteractions(getInitialPacts())
    }

    companion object {
        private const val TIMEOUT = 3L

        private const val PACT_ERROR_CODE = 999
        private val STATE_1 = ProviderState("State_1")
        private val STATE_2 = ProviderState("State_2")
    }
}