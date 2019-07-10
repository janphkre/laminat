package au.com.dius.pact.external

import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.LinkedList

/**
 * This is a web server which ignores any state specified in pacts and
 * matches the incoming request to any interaction that is specified at the moment.
 */
open class StatelessPactWebServer(allowUnexpectedKeys: Boolean, pactErrorCode: Int) {

    private val mockWebServer = MockWebServer()
    private val dispatcher = PactDispatcher(allowUnexpectedKeys, pactErrorCode)
    private val currentInteractionList = LinkedList<RequestResponseInteraction>()

    init {
        mockWebServer.setDispatcher(dispatcher)
        dispatcher.setInteractions(currentInteractionList)
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
        currentInteractionList.clear()
    }

    protected fun addCurrentInteractions(interactions: Collection<RequestResponseInteraction>) {
        currentInteractionList.addAll(interactions)
    }

    protected open fun updateInteractions(pact: RequestResponsePact) {
        addCurrentInteractions(pact.requestResponseInteractions)
    }
}