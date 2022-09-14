package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class RequestSpec: StringSpec({

    "delegates to the matching rules to parse matchers"() {
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
        val request = RequestResponsePactV3Deserializer().mapToRequest(json)

        // then:
        !request.matchingRules.isEmpty() shouldBe false
        request.matchingRules.hasCategory("stuff") shouldBe true
    }

    "fromMap sets defaults for attributes missing from the map" {
        // where:
        val request = RequestResponsePactV3Deserializer().mapToRequest(Json.Object())

        // expect:
        request.method shouldBe "GET"
        request.path shouldBe "/"
        request.query.isEmpty() shouldBe true
        request.headers.isEmpty() shouldBe true
        request.body should { it is OptionalBody.MissingBody }
        request.matchingRules.isEmpty() shouldBe true
        request.generators.isEmpty() shouldBe true

    }

    //TODO: NOT IMPLEMENTED YET
    /*"detects multipart file uploads based on the content type" {
        forAll(
            table(
                headers("contentType", "multipartFileUpload"),
                row("multipart/form-data", true),
                row("text/plain" , false),
                row("multipart/form-data; boundary=boundaryMarker", true),
                row("multipart/form-data;boundary=boundaryMarker", true),
                row("MULTIPART/FORM-DATA; boundary=boundaryMarker", true)
            )
        ) { contentType, multipartFileUpload ->
            Request(headers= mapOf("Content-Type" to contentType)).isMultipartFileUpload() shouldBe multipartFileUpload
        }
    }*/

    "handles the cookie header"() {
        // expect:
        Request(headers= mapOf("Cookie" to "test=12345; test2=abcd")).cookie() shouldBe listOf("test=12345", "test2=abcd")
    }

    "handles the cookie header with multiple values"() {
    // expect:
    Request(headers= mapOf("Cookie" to "test=12345; test2=abcd; test3=xgfes")).cookie() shouldBe listOf(
        "test=12345", "test2=abcd", "test3=xgfes"
    )
}
})