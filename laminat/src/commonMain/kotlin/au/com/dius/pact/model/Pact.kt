package au.com.dius.pact.model

import au.com.dius.pact.model.base.PactSource
import au.com.dius.pact.model.base.PactSpecVersion

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