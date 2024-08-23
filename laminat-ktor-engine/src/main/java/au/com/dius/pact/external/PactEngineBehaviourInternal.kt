package au.com.dius.pact.external

/**
 * Part of the behaviour that is being invoked by the pact engine.
 *
 * @author Jan Phillip Kretzschmar
 */
public interface PactEngineBehaviourInternal {
    val isActive: Boolean

    fun createMatch(request: IncomingRequest): RequestMatch

    fun countError()

    fun countMatch()
}