package au.com.dius.pact.model

import au.com.dius.pact.BuildConfig

abstract class BasePact: Pact {

    override val source: PactSource
        get() = UnknownPactSource

    override fun compatibleTo(other: Pact): Boolean {
        return provider == other.provider && this::class.isInstance(other)
    }

    companion object {

        fun getMetaData(version: String): Map<String, Any?> {
            return mapOf(
                Pair("pact-specification", mapOf(Pair("version", version))),
                Pair("pact-jvm", mapOf(Pair("version", BuildConfig.VERSION_NAME)))
            )
        }
    }
}