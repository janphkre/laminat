package au.com.dius.pact.external

import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.RequestResponsePact
import java.util.LinkedList

/**
 * This is a web server which handles states specified in pacts and set as a list of ProviderState.
 * it matches the incoming request to any interaction that is specified as part of the current provider state.
 *
 * @author Jan Phillip Kretzschmar
 */
class StatefullPactWebServer(
    internal val delegate: StatelessPactWebServer
) : PactWebServer {

    private val definedPactList = LinkedList<RequestResponsePact>()
    private var currentProviderStates: List<ProviderState> = emptyList()

    constructor(allowUnexpectedKeys: Boolean, pactErrorCode: Int) : this(
        StatelessPactWebServer(allowUnexpectedKeys, pactErrorCode)
    )

    override fun teardown() {
        delegate.teardown()
        definedPactList.clear()
        currentProviderStates = emptyList()
    }

    override fun addPact(pact: RequestResponsePact) {
        definedPactList.add(pact)
        updateInteractions(pact)
    }

    override fun addPacts(pacts: Collection<RequestResponsePact>) {
        definedPactList.addAll(pacts)
        pacts.forEach { updateInteractions(it) }
    }

    override fun clearPacts() {
        definedPactList.clear()
        delegate.clearPacts()
    }

    override fun getCurrentInteractionCount(): Int {
        return delegate.getCurrentInteractionCount()
    }

    override fun validateInteractionsCompleted(count: Long): Boolean {
        return delegate.validateInteractionsCompleted(count)
    }

    @Deprecated("Use validateInteractionsCompleted instead!")
    fun validatePactsCompleted(): Boolean {
        val calculateInteractionCount = definedPactList.fold(0L) { count, item ->
            count + item.requestResponseInteractions.size
        }
        return delegate.validateInteractionsCompleted(calculateInteractionCount)
    }

    override fun validateInteractionsCompleted(): Boolean {
        val calculateInteractionCount = definedPactList.fold(0L) { count, item ->
            count + item.requestResponseInteractions.size
        }
        return delegate.validateInteractionsCompleted(calculateInteractionCount)
    }

    override fun getUrlString(): String {
        return delegate.getUrlString()
    }

    override fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        return delegate.observeMatches(observer)
    }

    fun setStates(states: List<ProviderState>) {
        currentProviderStates = states
        delegate.clearCurrentInteractions()
        definedPactList.forEach { pact -> updateInteractions(pact) }
    }

    fun getDefinedPactCount(): Int {
        return definedPactList.size
    }

    fun getDefinedInteractionCount(): Int {
        return definedPactList.sumOf { it.requestResponseInteractions.size }
    }

    private fun updateInteractions(pact: RequestResponsePact) {
        delegate.addCurrentInteractions(pact.requestResponseInteractions.filter { interaction -> currentProviderStates.containsAll(interaction.providerStates) })
    }
}