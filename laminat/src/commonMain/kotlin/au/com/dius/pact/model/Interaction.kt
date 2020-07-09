package au.com.dius.pact.model

import au.com.dius.pact.model.base.PactSpecVersion

/**
 * Interface to an interaction between a consumer and a provider
 */
interface Interaction {
    /**
     * Interaction description
     */
    val description: String

    /**
     * This just returns the first description from getProviderStates()
     */
    @get:Deprecated("Use getProviderStates()")
    val providerState: String

    /**
     * Returns the provider states for this interaction
     */
    val providerStates: List<ProviderState>

    /**
     * Checks if this interaction conflicts with the other one. Used for merging au.com.dius.pact files.
     */
    fun conflictsWith(other: Interaction): Boolean

    /**
     * Converts this interaction to a Map
     */
    fun toMap(pactSpecVersion: PactSpecVersion): Map<*, *>

    fun toMap(): Map<*, *>

    fun uniqueKey(): String
}