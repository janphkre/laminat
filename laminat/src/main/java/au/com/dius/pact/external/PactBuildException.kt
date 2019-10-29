package au.com.dius.pact.external

/**
 * Exception to be thrown by the client when the pact could not be built successfully.
 * This may happen in extensions which use the real Api Client to create a pact from it.
 *
 * @author Jan Phillip Kretzschmar
 */
class PactBuildException(message: String, cause: Exception? = null) : Exception(message, cause)