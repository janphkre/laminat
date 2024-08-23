package au.com.dius.pact.model

import au.com.dius.pact.BuildConfig

abstract class BasePact : Pact {

    override var source: PactSource = PactSource.UnknownPactSource

    companion object {

        fun getMetaData(serializationConfig: PactSerializationConfig): Map<String, Any?> {
            val version = serializationConfig.specVersion.value
            return mutableMapOf(
                Pair("pact-specification", mapOf(Pair("version", version))),
                Pair("pact-laminat-android", mapOf(Pair("version", BuildConfig.VERSION_NAME))),
                Pair("pact-laminat-serialization-config", mapOf(Pair("maxLength", serializationConfig.truncateBinaryLength)))
            )
        }
    }
}