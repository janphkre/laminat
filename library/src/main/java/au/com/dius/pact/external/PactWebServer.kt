package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact

class PactWebServer {

    private val dispatcher = PactDispatcher()

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