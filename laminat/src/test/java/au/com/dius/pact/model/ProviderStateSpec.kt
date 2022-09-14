package au.com.dius.pact.model

import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class ProviderStateSpec: StringSpec({

    "generates a map of the state" {
        forAll(
            table(
                headers("state", "map"),
                row(ProviderState("test"), mapOf<String,Any>("name" to "test")),
                row(ProviderState("test", mapOf()), mapOf<String, Any>("name" to "test")),
                row(ProviderState("test", mapOf("a" to "B")), mapOf("name" to "test", "params" to mapOf("a" to "B"))),
            )
        ) { state, map ->

            state.toMap() shouldBe map
        }
    }
})