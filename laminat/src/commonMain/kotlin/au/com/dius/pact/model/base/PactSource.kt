package au.com.dius.pact.model.base

/**
 * Represents the source of a Pact
 */
sealed class PactSource {
    open fun description() = toString()

    object UnknownPactSource : PactSource()
}