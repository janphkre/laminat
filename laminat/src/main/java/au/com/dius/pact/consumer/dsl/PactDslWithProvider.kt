package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.model.ProviderState
import java.util.*

class PactDslWithProvider(private val consumerPactBuilder: ConsumerPactBuilder, private val providerName: String) {

    /**
     * Describe the state the provider needs to be in for the au.com.dius.pact test to be verified.
     *
     * @param state Provider state
     */
    fun given(state: String): PactDslWithState {
        return PactDslWithState(
            consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
            ProviderState(state)
        )
    }

    /**
     * Describe the state the provider needs to be in for the au.com.dius.pact test to be verified.
     *
     * @param state Provider state
     * @param params Data parameters for the state
     */
    fun given(state: String, params: Map<String, Any>): PactDslWithState {
        return PactDslWithState(
            consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
            ProviderState(state, params)
        )
    }

    /**
     * Describe the state the provider needs to be in for the au.com.dius.pact test to be verified.
     *
     * @param firstKey Key of first parameter element
     * @param firstValue Value of first parameter element
     * @param paramsKeyValuePair Additional parameters in key-value pairs
     */
    fun given(state: String?, firstKey: String, firstValue: Any, vararg paramsKeyValuePair: Any): PactDslWithState {
        require(paramsKeyValuePair.size % 2 == 0) { "Pair key value should be provided, but there is one key without value." }
        val params: MutableMap<String, Any> = HashMap()
        params[firstKey] = firstValue
        var i = 0
        while (i < paramsKeyValuePair.size) {
            params[paramsKeyValuePair[i].toString()] = paramsKeyValuePair[i + 1]
            i += 2
        }
        return PactDslWithState(
            consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
            ProviderState(state!!, params)
        )
    }

    /**
     * Description of the request that is expected to be received
     *
     * @param description request description
     */
    fun uponReceiving(description: String): PactDslRequestWithoutPath {
        return PactDslWithState(consumerPactBuilder, consumerPactBuilder.consumerName, providerName)
            .uponReceiving(description)
    }
}