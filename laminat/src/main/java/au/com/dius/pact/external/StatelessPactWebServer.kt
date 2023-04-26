package au.com.dius.pact.external

import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import java.io.IOException
import okhttp3.mockwebserver.MockWebServer

/**
 * This is a web server which ignores any state specified in pacts and
 * matches the incoming request to any interaction that is specified at the moment.
 *
 * @author Jan Phillip Kretzschmar
 */
class StatelessPactWebServer(allowUnexpectedKeys: Boolean, pactErrorCode: Int): PactWebServer {

    internal val mockWebServer = MockWebServer()
    internal val dispatcher = PactDispatcher(allowUnexpectedKeys, pactErrorCode)
    private var currentInteractionList: List<RequestResponseInteraction> = emptyList()

    init {
        mockWebServer.dispatcher = dispatcher
    }

    override fun teardown() {
        try {
            mockWebServer.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        clearPacts()
    }

    override fun addPact(pact: RequestResponsePact) {
        addCurrentInteractions(pact.requestResponseInteractions)
    }

    override fun addPacts(pacts: Collection<RequestResponsePact>) {
        pacts.forEach { addCurrentInteractions(it.requestResponseInteractions) }
    }

    override fun clearPacts() {
        clearCurrentInteractions()
        dispatcher.clearPactCompletions()
    }

    override fun getCurrentInteractionCount(): Int {
        return currentInteractionList.size
    }

    @Deprecated("Use validateInteractionsCompleted instead!", ReplaceWith("validateInteractionsCompleted(count)"))
    fun validatePactsCompleted(count: Long): Boolean {
        return validateInteractionsCompleted(count)
    }

    override fun validateInteractionsCompleted(count: Long): Boolean {
        return dispatcher.validateInteractionsCompleted(count)
    }

    override fun validateInteractionsCompleted(): Boolean {
        return validateInteractionsCompleted(currentInteractionList.size.toLong())
    }

    override fun getUrlString(): String {
        return mockWebServer.url("").toString()
    }

    fun clearCurrentInteractions() {
        synchronized(this) {
            currentInteractionList = emptyList()
            dispatcher.setInteractions(currentInteractionList)
        }
    }

    fun addCurrentInteractions(interactions: Collection<RequestResponseInteraction>) {
        synchronized(this) {
            currentInteractionList = currentInteractionList.plus(interactions)
            dispatcher.setInteractions(currentInteractionList)
        }
    }

    override fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        dispatcher.setMatchObserver(observer)
    }
}