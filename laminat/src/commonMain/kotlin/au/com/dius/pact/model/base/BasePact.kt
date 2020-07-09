package au.com.dius.pact.model.base

import au.com.dius.pact.model.Pact

abstract class BasePact : Pact {

    override val source: PactSource
        get() = PactSource.UnknownPactSource

    companion object {

        fun getMetaData(version: String): Map<String, Any?> {
            return mapOf(
                Pair("pact-specification", mapOf(Pair("version", version))),
                Pair("pact-laminat", mapOf(Pair("version", BuildConfig.VERSION_NAME)))
            )
        }
    }
}