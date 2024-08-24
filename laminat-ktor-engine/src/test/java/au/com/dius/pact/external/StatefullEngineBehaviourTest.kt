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

class StatefullEngineBehaviourTest: AbstractRequestTest() {

    private lateinit var mockEngineBehaviour: StatefullEngineBehaviour

    @Before
    fun setup() {
        mockEngineBehaviour = StatefullEngineBehaviour()
        mockEngineBehaviour.addPacts(getInitialPacts())
        mockEngineBehaviour.observeMatches { incomingRequest, requestMatch ->
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
    fun statefulBehaviourStarted_noStateSet_returnsCorrectCounts() {
        Assert.assertEquals(1, mockEngineBehaviour.getDefinedPactCount())
        Assert.assertEquals(2, mockEngineBehaviour.getDefinedInteractionCount())
        Assert.assertEquals(0, mockEngineBehaviour.getCurrentInteractionCount())
        Assert.assertEquals(false, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourStarted_combinedStateSet_returnsCorrectCounts() {
        mockEngineBehaviour.setStates(listOf(STATE_1, STATE_2))

        Assert.assertEquals(1, mockEngineBehaviour.getDefinedPactCount())
        Assert.assertEquals(2, mockEngineBehaviour.getDefinedInteractionCount())
        Assert.assertEquals(2, mockEngineBehaviour.getCurrentInteractionCount())
        Assert.assertEquals(false, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourStarted_singleStateSet_returnsCorrectCounts() {
        mockEngineBehaviour.setStates(listOf(STATE_2))

        Assert.assertEquals(1, mockEngineBehaviour.getDefinedPactCount())
        Assert.assertEquals(2, mockEngineBehaviour.getDefinedInteractionCount())
        Assert.assertEquals(1, mockEngineBehaviour.getCurrentInteractionCount())
        Assert.assertEquals(false, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourStarted_pactsCleared_returnsCorrectCounts() {
        mockEngineBehaviour.clearPacts()

        Assert.assertEquals(0, mockEngineBehaviour.getDefinedPactCount())
        Assert.assertEquals(0, mockEngineBehaviour.getDefinedInteractionCount())
        Assert.assertEquals(0, mockEngineBehaviour.getCurrentInteractionCount())
        Assert.assertEquals(true, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourStarted_singleStateSet_matchesRequestCorrectly() {
        mockEngineBehaviour.setStates(listOf(STATE_1))

        Assert.assertEquals(true, mockEngineBehaviour.createMatch(createIncomingRequest(method = "GET")) is RequestMatch.FullRequestMatch)
        Assert.assertEquals(false, mockEngineBehaviour.createMatch(createIncomingRequest(
            requestBody = "null".toByteArray(),
            method = "POST",
            contentType = "application/json; charset=UTF-8"
        )) is RequestMatch.FullRequestMatch)
    }

    @Test
    fun statefulBehaviourStarted_combinedStatesSetRequested_validatesAllPacts() {
        mockEngineBehaviour.setStates(listOf(STATE_1, STATE_2))

        Assert.assertEquals(true, mockEngineBehaviour.createMatch(createIncomingRequest(method = "GET")) is RequestMatch.FullRequestMatch)
        Assert.assertEquals(true, mockEngineBehaviour.createMatch(createIncomingRequest(
            requestBody = "null".toByteArray(),
            method = "POST",
            contentType = "application/json; charset=UTF-8"
        )) is RequestMatch.FullRequestMatch)
    }

    @Test
    fun statefulBehaviourInvoked_countsMatchedInteractions_validatesInteractions() {
        mockEngineBehaviour.setStates(listOf(STATE_1))
        mockEngineBehaviour.countMatch()
        mockEngineBehaviour.countMatch()
        Assert.assertEquals(true, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourInvoked_countsMatchedInteractions_failsToValidateInteractions() {
        mockEngineBehaviour.setStates(listOf(STATE_1))
        mockEngineBehaviour.countMatch()
        Assert.assertEquals(false, mockEngineBehaviour.validateInteractionsCompleted())
    }

    @Test
    fun statefulBehaviourInvoked_countsMismatchedInteractions_failsToValidateInteractions() {
        mockEngineBehaviour.setStates(listOf(STATE_1))
        mockEngineBehaviour.countMatch()
        mockEngineBehaviour.countMatch()
        mockEngineBehaviour.countError()
        Assert.assertEquals(false, mockEngineBehaviour.validateInteractionsCompleted())
    }

    companion object {
        private val STATE_1 = ProviderState("State_1")
        private val STATE_2 = ProviderState("State_2")
    }
}