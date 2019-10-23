package au.com.dius.pact.model

/**
 * Pact Provider
 */
data class Provider @JvmOverloads constructor(val name: String = "provider") {

    fun toMap(): Map<String, Any?> {
        return mapOf(Pair("name", name))
    }
}

/**
 * Pact Consumer
 */
data class Consumer @JvmOverloads constructor(val name: String = "consumer") {

    fun toMap(): Map<String, Any?> {
        return mapOf(Pair("name", name))
    }
}

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

/**
 * Interface to a au.com.dius.pact
 */
interface Pact {
    /**
     * Returns the provider of the service for the au.com.dius.pact
     */
    val provider: Provider
    /**
     * Returns the consumer of the service for the au.com.dius.pact
     */
    val consumer: Consumer
    /**
     * Returns all the interactions of the au.com.dius.pact
     */
    val interactions: List<Interaction>

    /**
     * The source that this au.com.dius.pact was loaded from
     */
    val source: PactSource

    /**
     * Returns a au.com.dius.pact with the interactions sorted
     */
    fun sortInteractions(): Pact

    /**
     * Returns a Map representation of this au.com.dius.pact for the purpose of generating a JSON document.
     */
    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, *>

    /**
     * If this au.com.dius.pact is compatible with the other au.com.dius.pact. Pacts are compatible if they have the
     * same provider and they are the same type
     */
    fun compatibleTo(other: Pact): Boolean

    /**
     * If this au.com.dius.pact is conflicting with the other au.com.dius.pact. Pacts are conflicting if they have a conflicting interaction
     */
    fun conflictsWith(other: Pact): List<Pair<Interaction, Interaction>>

    /**
     * If this au.com.dius.pact has conflicting interactions within himself
     */
    fun conflictsWithSelf(): List<Pair<Interaction, Interaction>>

    /**
     * Merges all the interactions into this Pact
     * @param interactions
     */
    fun mergeInteractions(interactions: List<Interaction>)

    /**
     * Returns a new Pact with all the interactions filtered by the provided predicate
     * @deprecated Wrap the au.com.dius.pact in a FilteredPact instead
     */
    @Deprecated("Wrap the au.com.dius.pact in a FilteredPact instead")
    fun filterInteractions(predicate: (Interaction) -> Boolean): Pact
}