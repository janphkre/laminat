package au.com.dius.pact.external

import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact

abstract class BaseEngineBehaviour: PactEngineBehaviour, PactEngineBehaviourInternal {

    var interactionList: List<RequestResponseInteraction> = emptyList()
    private var pactMatcher = RequestMatcher(FeatureFlags.isFeatureEnabled(Feature.ALLOW_UNEXPECTED_KEYS))

    override var isActive: Boolean = true
    private var matchObserver: ((IncomingRequest, RequestMatch) -> Unit)? = null
    private var matchedRequestCount: Long = 0L
    private var unmatchedRequestsCount: Long = 0L

    override fun teardown() {
        isActive = false
        clearPacts()
    }

    override fun addPact(pact: RequestResponsePact) {
        addInteractions(pact.requestResponseInteractions)
    }

    override fun addPacts(pacts: Collection<RequestResponsePact>) {
        pacts.forEach { addInteractions(it.requestResponseInteractions) }
    }

    override fun clearPacts() {
        synchronized(this) {
            interactionList = emptyList()
        }
        clearPactCompletions()
    }

    override fun clearPactCompletions() {
        matchedRequestCount = 0L
        unmatchedRequestsCount = 0L
    }

    override fun getCurrentInteractionCount(): Int {
        return interactionList.size
    }

    override fun validateInteractionsCompleted(count: Long): Boolean {
        return matchedRequestCount == count && unmatchedRequestsCount == 0L
    }

    override fun validateInteractionsCompleted(): Boolean {
        return validateInteractionsCompleted(interactionList.size.toLong())
    }

    override fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?) {
        matchObserver = observer
    }

    open fun addInteractions(interactions: Collection<RequestResponseInteraction>) {
        synchronized(this) {
            interactionList = interactionList.plus(interactions)
        }
    }

    override fun countError() {
        unmatchedRequestsCount++
    }

    override fun countMatch() {
        matchedRequestCount++
    }

    override fun createMatch(request: IncomingRequest): RequestMatch {
        val match = pactMatcher.findInteraction(interactionList, request)
        matchObserver?.invoke(request, match)
        return match
    }
}
