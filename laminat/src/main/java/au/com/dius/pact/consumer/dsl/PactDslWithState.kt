package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.model.ProviderState
import java.util.*

class PactDslWithState internal constructor(private val consumerPactBuilder: ConsumerPactBuilder?, var consumerName: String?, var providerName: String?) {
    var state: MutableList<ProviderState>

    internal constructor(consumerPactBuilder: ConsumerPactBuilder?, consumerName: String?, providerName: String?, state: ProviderState) : this(
        consumerPactBuilder,
        consumerName,
        providerName
    ) {
        this.state.add(state)
    }

    /**
     * Description of the request that is expected to be received
     *
     * @param description request description
     */
    fun uponReceiving(description: String?): PactDslRequestWithoutPath {
        return PactDslRequestWithoutPath(consumerPactBuilder!!, this, description!!)
    }

    /**
     * Adds another provider state to this interaction
     * @param stateDesc Description of the state
     */
    fun given(stateDesc: String?): PactDslWithState {
        state.add(ProviderState(stateDesc))
        return this
    }

    /**
     * Adds another provider state to this interaction
     * @param stateDesc Description of the state
     * @param params State data parameters
     */
    fun given(stateDesc: String?, params: Map<String?, Any?>?): PactDslWithState {
        state.add(ProviderState(stateDesc!!, params))
        return this
    }

    init {
        state = ArrayList()
    }
}