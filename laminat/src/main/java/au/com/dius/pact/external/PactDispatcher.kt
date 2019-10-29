package au.com.dius.pact.external

import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.Response
import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.apache.http.Consts
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * An okhttp dispatcher that creates matches based on the list of interactions through an OkHttpRequestMatcher.
 * These matches are converted to a single MockResponse. If there are conflicts or multiple matches
 * an error response with the given pactErrorCode is created which describes the Mismatch.
 *
 * If you want to create a self contained mock server to fully mirror the real server
 * you can register a matchObserver to perform changes.
 * To verify that a expected request count has been received the method validatePactsCompleted may be invoked.
 *
 * @author Jan Phillip Kretzschmar
 */
internal class PactDispatcher(allowUnexpectedKeys: Boolean, private val pactErrorCode: Int) : Dispatcher() {

    private var pactMatcher = OkHttpRequestMatcher(allowUnexpectedKeys)
    private var interactionList = emptyList<RequestResponseInteraction>()
    private var matchedRequestCount: Long = 0L
    private var unmatchedRequestsCount: Long = 0L
    private var matchObserver: ((IncomingRequest, RequestMatch) -> Unit)? = null

    fun setMatchObserver(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        matchObserver = observer
    }

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
        if (request == null) {
            return notFoundMockResponse()
        }
        try {
            val incomingRequest = IncomingRequest(request)
            val requestMatch = pactMatcher.findInteraction(interactionList, incomingRequest)
            matchObserver?.invoke(incomingRequest, requestMatch)
            return when (requestMatch) {
                is RequestMatch.FullRequestMatch -> {
                    matchedRequestCount++
                    requestMatch.interaction.response.generateResponse().mapToMockResponse()
                }
                is RequestMatch.PartialRequestMatch -> {
                    notFoundMockResponse().setBody("Partially matched ${requestMatch.interaction.uniqueKey()}:\n${requestMatch.problems.joinToString("\n")}")
                }
                is RequestMatch.RequestMismatch -> {
                    notFoundMockResponse().setBody("Failed to match request at all! Best match was with ${requestMatch.interaction?.uniqueKey()}:\n" +
                        "${requestMatch.problems?.joinToString("\n")}")
                }
            }
        } catch (e: PactMergeException) {
            return notFoundMockResponse().setBody(e.message)
        } catch (e: Exception) {
            ByteArrayOutputStream().use { outputStream ->
                PrintStream(outputStream, true, Consts.UTF_8.name()).use { printStream ->
                    e.printStackTrace(printStream)
                }
                return notFoundMockResponse().setBody(outputStream.toString(Consts.UTF_8.name()))
            }
        }
    }

    private fun Response.mapToMockResponse(): MockResponse {
        return MockResponse()
            .setResponseCode(this.status)
            .setHeaders(this.headers.mapToMockHeaders())
            .setBody(this.body.value ?: "")
    }

    private fun Map<String, String>?.mapToMockHeaders(): Headers {
        if (this == null) {
            return Headers.of()
        }
        return Headers.of(this)
    }

    private fun notFoundMockResponse(): MockResponse {
        unmatchedRequestsCount++
        return MockResponse().setResponseCode(pactErrorCode)
    }
}