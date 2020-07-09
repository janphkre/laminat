package au.com.dius.pact.external

import au.com.dius.pact.model.requests.RequestResponseInteraction
import au.com.dius.pact.model.requests.RequestResponsePact
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

/**
 * This is a web server which ignores any state specified in pacts and
 * matches the incoming request to any interaction that is specified at the moment.
 *
 * @author Jan Phillip Kretzschmar
 */
open class StatelessPactWebServer(allowUnexpectedKeys: Boolean, pactErrorCode: Int) {

    internal val mockWebServer = MockWebServer()
    internal val dispatcher = PactDispatcher(allowUnexpectedKeys, pactErrorCode)
    private var currentInteractionList: List<RequestResponseInteraction> = emptyList()

    init {
        mockWebServer.setDispatcher(dispatcher)
    }

    open fun teardown() {
        try {
            mockWebServer.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        clearPacts()
    }

    open fun addPact(pact: RequestResponsePact) {
        updateInteractions(pact)
    }

    open fun addPacts(pacts: Collection<RequestResponsePact>) {
        pacts.forEach { updateInteractions(it) }
    }

    open fun clearPacts() {
        clearCurrentInteractions()
        dispatcher.clearPactCompletions()
    }

    fun getCurrentInteractionCount(): Int {
        return currentInteractionList.size
    }

    fun validatePactsCompleted(count: Long): Boolean {
        return dispatcher.validatePactsCompleted(count)
    }

    fun getUrlString(): String {
        return mockWebServer.url("").toString()
    }

    protected fun clearCurrentInteractions() {
        synchronized(this) {
            currentInteractionList = emptyList()
            dispatcher.setInteractions(currentInteractionList)
        }
    }

    protected fun addCurrentInteractions(interactions: Collection<RequestResponseInteraction>) {
        synchronized(this) {
            currentInteractionList = currentInteractionList.plus(interactions)
            dispatcher.setInteractions(currentInteractionList)
        }
    }

    protected open fun updateInteractions(pact: RequestResponsePact) {
        addCurrentInteractions(pact.requestResponseInteractions)
    }

    fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        dispatcher.setMatchObserver(observer)
    }
}