package au.com.dius.pact.external

import au.com.dius.pact.model.*
import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.LinkedHashSet
import java.util.LinkedList

internal class PactDispatcher(allowUnexpectedKeys: Boolean): Dispatcher() {

    private val notImplementedCode = 501
    private var pactMatcher = OkHttpRequestMatcher(allowUnexpectedKeys)
    private var interactionList = emptyList<RequestResponseInteraction>()
    private var matchedRequestCount: Long = 0L
    private var unmatchedRequestsCount: Long = 0L

    fun setInteractions(interactions: List<RequestResponseInteraction>) {
        interactionList = interactions
    }

    fun clearPactCompletions() {
        matchedRequestCount = 0L
        unmatchedRequestsCount = 0L
    }

    fun validatePactsCompleted(count: Long): Boolean {
        return matchedRequestCount == count && unmatchedRequestsCount == 0L
    }

    override fun dispatch(request: RecordedRequest?): MockResponse {
        if(request == null) {
            return notFoundMockResponse()
        }
        val bodyString = request.body.inputStream().use {
            it.bufferedReader().readText()
        }
        try {
            val requestMatch = pactMatcher.findInteraction(interactionList, request)
            return when (requestMatch) {
                is OkHttpRequestMatcher.RequestMatch.FullRequestMatch ->  {
                    matchedRequestCount++
                    requestMatch.interaction.response.generateResponse().mapToMockResponse()
                }
                is OkHttpRequestMatcher.RequestMatch.PartialRequestMatch -> {
                    notFoundMockResponse().setBody(requestMatch.problems.joinToString("\n"))
                }
                else -> {
                    notFoundMockResponse()
                }
            }
        } catch(e: PactMergeException) {
            return notFoundMockResponse().setBody(e.message)
        }
        //TODO: ADD ADDITIONAL try catch to not crash app when an error happens.
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
        return MockResponse().setResponseCode(notImplementedCode)
    }
}
