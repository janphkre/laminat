package au.com.dius.pact.model

data class PactSerializationConfig(
    val specVersion: PactSpecVersion,
    val truncateBinaryLength: Int?
)