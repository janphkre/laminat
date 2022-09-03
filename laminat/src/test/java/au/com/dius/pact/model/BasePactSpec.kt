package au.com.dius.pact.model

import au.com.dius.pact.BuildConfig
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

// BasePactSpec.groovy in pact-jvm
class BasePactSpec : StringSpec({

    "metadata should use the metadata from the pact file as a base" {
        BasePact.getMetaData(PactSerializationConfig(PactSpecVersion.V3, 12)) shouldBe mapOf(
            "pact-specification" to mapOf("version" to "3.0.0"),
            "pact-laminat-android" to mapOf("version" to BuildConfig.VERSION_NAME),
            "pact-laminat-serialization-config" to mapOf("maxLength" to 12)
        )
    }
})