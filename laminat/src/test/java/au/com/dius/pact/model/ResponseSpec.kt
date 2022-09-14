package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class ResponseSpec: StringSpec({
    "delegates to the matching rules to parse matchers" {
        // given:
        val json = Json.Object(
            "matchingRules" to Json.Object(
                "stuff" to Json.Object(
                    "" to Json.Object(
                        "matchers" to Json.Array(
                            Json.Object("match" to Json.wrapInJson("type"))
                        )
                    )
                )
            )
        )

        // when:
        val response = RequestResponsePactV3Deserializer().mapToResponse(json)

        // then:
        response.matchingRules.isEmpty() shouldBe false
        response.matchingRules.hasCategory("stuff") shouldBe true
    }
})