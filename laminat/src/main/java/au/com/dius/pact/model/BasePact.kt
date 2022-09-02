package au.com.dius.pact.model

import au.com.dius.pact.BuildConfig

abstract class BasePact : Pact {

    override val source: PactSource
        get() = UnknownPactSource

    companion object {

        fun getMetaData(serializationConfig: PactSerializationConfig): Map<String, Any?> {
            val version = if (serializationConfig.specVersion >= PactSpecVersion.V3) "3.0.0" else "2.0.0"
            return mapOf(
                Pair("pact-specification", mapOf(Pair("version", version))),
                Pair("pact-laminat-android", mapOf(Pair("version", BuildConfig.VERSION_NAME))),
                Pair("pact-laminat-binary-truncation", mapOf(Pair("maxLength", serializationConfig.truncateBinaryLength)))
            )
        }
    }
}