package au.com.dius.pact.external

import au.com.dius.pact.model.RequestResponsePact

/**
 * This is a web server using pacts to reply to incoming requests.
 *
 * @author Jan Phillip Kretzschmar
 */
public interface PactEngineBehaviour: PactEngineBehaviourInternal {
    /**
     * Destroys the engine.
     */
    fun teardown()

    /**
     * Add a pact to the server to use when matching incoming requests.
     */
    fun addPact(pact: RequestResponsePact)

    /**
     * Add pacts to the server to use when matching incoming requests.
     */
    fun addPacts(pacts: Collection<RequestResponsePact>)

    /**
     * Clear all pacts that are used when matching incoming requests.
     */
    fun clearPacts()

    /**
     * Clears the recording count of pacts.
     */
    fun clearPactCompletions()

    /**
     * Return the count of active interactions in the server used to match incoming requests.
     */
    fun getCurrentInteractionCount(): Int

    /**
     * Validates that a given [count] of interactions was completed.
     */
    fun validateInteractionsCompleted(count: Long): Boolean

    /**
     * Validates that the number of matches matches the number of stored interactions.
     */
    fun validateInteractionsCompleted(): Boolean

    /**
     * Sets observer that will be invoked when a incoming request was matched against the current interactions.
     */
    fun observeMatches(observer: ((IncomingRequest, RequestMatch) -> Unit)?)
}