package au.com.dius.pact.model

import au.com.dius.pact.BuildConfig
import com.google.gson.JsonParser

abstract class BasePact : Pact {

    override val source: PactSource
        get() = UnknownPactSource

    override fun compatibleTo(other: Pact): Boolean {
        return provider == other.provider && this::class.isInstance(other)
    }

    companion object {

        val jsonParser = JsonParser()

        fun getMetaData(version: String): Map<String, Any?> {
            return mapOf(
                Pair("pact-specification", mapOf(Pair("version", version))),
                Pair("pact-laminat-android", mapOf(Pair("version", BuildConfig.VERSION_NAME)))
            )
        }
    }
}