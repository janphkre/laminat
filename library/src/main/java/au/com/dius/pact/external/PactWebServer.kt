package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

class PactWebServer(allowUnexpectedKeys: Boolean) {

    private val mockWebServer = MockWebServer()
    private val dispatcher = PactDispatcher(allowUnexpectedKeys)

    init {
        mockWebServer.setDispatcher(dispatcher)
    }

    fun teardown() {
        try {
            mockWebServer.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        dispatcher.clearPacts()
    }

    fun addPact(pact: RequestResponsePact) {
        dispatcher.addPact(pact)
    }

    fun addPacts(pacts: Collection<RequestResponsePact>) {
        dispatcher.addPacts(pacts)
    }

    fun clearPacts() {
        dispatcher.clearPacts()
    }

    fun setStates(states: List<ProviderState>) {
        dispatcher.setStates(states)
    }

    fun validatePactComplete(): Boolean {
        return dispatcher.validatePactComplete()
    }
}