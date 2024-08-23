package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import java.util.LinkedList

class StatefullEngineBehaviour: BaseEngineBehaviour() {

    private val definedPactList = LinkedList<RequestResponsePact>()
    private var currentProviderStates: List<ProviderState> = emptyList()

    override fun teardown() {
        super.teardown()
        currentProviderStates = emptyList()
    }

    override fun clearPacts() {
        definedPactList.clear()
        super.clearPacts()
    }

    override fun addPact(pact: RequestResponsePact) {
        definedPactList.add(pact)
        updateInteractions(pact)
    }

    override fun addPacts(pacts: Collection<RequestResponsePact>) {
        definedPactList.addAll(pacts)
        pacts.forEach { updateInteractions(it) }
    }

    override fun validateInteractionsCompleted(): Boolean {
        val calculatedInteractionCount = definedPactList.fold(0L) { count, item ->
            count + item.requestResponseInteractions.size
        }
        return super.validateInteractionsCompleted(calculatedInteractionCount)
    }

    fun setStates(states: List<ProviderState>) {
        currentProviderStates = states
        super.clearPacts()
        definedPactList.forEach { pact -> updateInteractions(pact) }
    }

    fun getDefinedPactCount(): Int {
        return definedPactList.size
    }

    fun getDefinedInteractionCount(): Int {
        return definedPactList.sumOf { it.requestResponseInteractions.size }
    }

    private fun updateInteractions(pact: RequestResponsePact) {
        addInteractions(pact.requestResponseInteractions.filter { interaction -> currentProviderStates.containsAll(interaction.providerStates) })
    }
}