package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.Response
import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.LinkedHashSet
import java.util.LinkedList

internal class PactDispatcher: Dispatcher() {

    private val notFoundResponseCode = 501
    private val pactMatcher = OkHttpRequestMatcher()
    private val definedPactList = LinkedList<RequestResponsePact>()
    private var currentProviderStates: List<ProviderState> = emptyList()
    private val currentInteractionList = LinkedList<RequestResponseInteraction>()
    private var matchedRequestCount: Long = 0L
    private var unmatchedRequestsCount: Long = 0L

    fun addPact(pact: RequestResponsePact) {
        definedPactList.add(pact)
        calculateInteractions(pact)
    }

    fun addPacts(pacts: Collection<RequestResponsePact>) {
        definedPactList.addAll(pacts)
        pacts.forEach { pact -> calculateInteractions(pact) }
    }

    fun clearPacts() {
        definedPactList.clear()
        currentInteractionList.clear()
        matchedRequestCount = 0L
        unmatchedRequestsCount = 0L
    }

    fun setStates(states: List<ProviderState>) {
        currentProviderStates = states
        currentInteractionList.clear()
        definedPactList.forEach { pact -> calculateInteractions(pact) }
    }

    fun validatePactComplete(): Boolean {
        val calculateInteractionCount = definedPactList.fold(0L) { count, item ->
            count + item.requestResponseInteractions.size
        }
        return matchedRequestCount == calculateInteractionCount && unmatchedRequestsCount == 0L
    }

    override fun dispatch(request: RecordedRequest?): MockResponse {
        if(request == null) {
            return notFoundMockResponse()
        }
        val bodyString = request.body.inputStream().use {
            it.bufferedReader().readText()
        }
        //TODO: TRY CATCH:
        val interaction = pactMatcher.findInteraction(currentInteractionList, request)
        matchedRequestCount++
        return interaction?.response?.generateResponse()?.mapToMockResponse() ?: notFoundMockResponse()
    }

    private fun Response.mapToMockResponse(): MockResponse {
        return MockResponse()
            .setResponseCode(this.status)
            .setHeaders(this.headers.mapToMockHeaders())
            .setBody(this.body.value ?: "")
    }

    private fun Map<String, String>?.mapToMockHeaders(): Headers {
        if(this == null) {
            return Headers.of()
        }
        return Headers.of(this)
    }

    private fun notFoundMockResponse() : MockResponse {
        unmatchedRequestsCount++
        return MockResponse().setResponseCode(notFoundResponseCode)
    }

    private fun calculateInteractions(pact: RequestResponsePact) {
        currentInteractionList.addAll(pact.requestResponseInteractions.filter { interaction -> currentProviderStates.containsAll(interaction.providerStates) })
    }
}
