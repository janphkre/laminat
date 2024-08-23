package au.com.dius.pact.model

import au.com.dius.pact.matchers.MatchingConfig
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

// ContentTypeSpec.groovy in pact-jvm
class MatchingConfigSpec : StringSpec({

    "#value is json -> #result" {
        forAll(
            table(
                headers("value", "result"),
                row("", false),
                row("test/plain", false),
                row("application/pdf", false),
                row("application/xml", false),
                row("application/json", true),
                row("application/hal+json", true),
                row("application/HAL+JSON", true)
            )
        ) { value, result ->
            MatchingConfig.isJson(value) shouldBe result
        }
    }

    "#value is xmk -> #result" {
        forAll(
            table(
                headers("value", "result"),
                row("", false),
                row("test/plain", false),
                row("application/pdf", false),
                row("application/json", false),
                row("application/xml", true),
                row("application/stuff+xml", true),
                row("application/STUFF+XML", true)
            )
        ) { value, result ->
            MatchingConfig.isXml(value) shouldBe result
        }
    }
})