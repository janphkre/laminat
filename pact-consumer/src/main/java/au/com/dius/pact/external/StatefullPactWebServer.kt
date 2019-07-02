package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import java.util.LinkedList

class StatefullPactWebServer(allowUnexpectedKeys: Boolean, pactErrorCode: Int) : StatelessPactWebServer(allowUnexpectedKeys, pactErrorCode) {

    private val definedPactList = LinkedList<RequestResponsePact>()
    private var currentProviderStates: List<ProviderState> = emptyList()

    override fun teardown() {
        super.teardown()
        definedPactList.clear()
        currentProviderStates = emptyList()
    }

    override fun addPact(pact: RequestResponsePact) {
        definedPactList.add(pact)
        super.addPact(pact)
    }

    override fun addPacts(pacts: Collection<RequestResponsePact>) {
        definedPactList.addAll(pacts)
        super.addPacts(pacts)
    }

    override fun clearPacts() {
        definedPactList.clear()
        super.clearPacts()
    }

    fun validatePactsCompleted(): Boolean {
        val calculateInteractionCount = definedPactList.fold(0L) { count, item ->
            count + item.requestResponseInteractions.size
        }
        return super.validatePactsCompleted(calculateInteractionCount)
    }

    fun setStates(states: List<ProviderState>) {
        currentProviderStates = states
        clearCurrentInteractions()
        definedPactList.forEach { pact -> updateInteractions(pact) }
    }

    fun getDefinedPactCount(): Int {
        return definedPactList.size
    }

    fun getDefinedInteractionCount(): Int {
        return definedPactList.sumBy { it.requestResponseInteractions.size }
    }

    override fun updateInteractions(pact: RequestResponsePact) {
        addCurrentInteractions(pact.requestResponseInteractions.filter { interaction -> currentProviderStates.containsAll(interaction.providerStates) })
    }
}