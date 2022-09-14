package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class ProviderAndConsumerSpec: StringSpec({

    // different from pact-jvm: empty string is treated as a valid name
    "creates a provider from a Map" {
        forAll(
            table(
                headers("json", "provider"),
                row(Json.Object(), Provider("provider")),
                row(Json.Object("name" to Json.Null), Provider("provider")),
                row(Json.Object("name" to Json.wrapInJson("")), Provider("")),
                row(Json.Object("name" to Json.wrapInJson("test")), Provider("test")),
            )
        ) { json, provider ->
            Provider.fromJson(json) shouldBe provider
        }
    }

    // different from pact-jvm: empty string is treated as a valid name
    "creates a consumer from a Map" {
        forAll(
            table(
                headers("json", "consumer"),
                row(Json.Object("name" to Json.Null), Consumer("consumer")),
                row(Json.Object("name" to Json.wrapInJson("")), Consumer("")),
                row(Json.Object("name" to Json.wrapInJson("test")), Consumer("test")),
            )
        ) { json, consumer ->
            Consumer.fromJson(json) shouldBe consumer
        }
    }
})