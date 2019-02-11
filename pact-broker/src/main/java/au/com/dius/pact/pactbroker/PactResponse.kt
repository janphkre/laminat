package au.com.dius.pact.pactbroker

/**
 * Wraps the response for a Pact from the broker with the link data associated with the Pact document.
 */
data class PactResponse(val pactFile: Any, val links: Map<String, Map<String, Any>>)