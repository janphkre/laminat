package au.com.dius.pact.model

import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec
import org.apache.http.Consts

class HttpPartSpec : StringSpec({

    "Pact mimeType" {
        forAll(
            table(
                headers("request", "mimeType"),
                row(Request(), "text/plain"),
                row(Request("Get", ""), "text/plain"),
                row(Request("Get", "", emptyMap()), "text/plain"),
                row(Request("Get", "", headers = mapOf("Content-Type" to "text/html")), "text/html"),
                row(Request("Get", "", emptyMap(), mapOf("Content-Type" to "text/html")), "text/html"),
                row(Request("Get", "", emptyMap(), mapOf("Content-Type" to "application/json; charset=UTF-8")), "application/json"),
                row(Request("Get", "", emptyMap(), mapOf("content-type" to "application/json")), "application/json"),
                row(Request("Get", "", emptyMap(), mapOf("CONTENT-TYPE" to "application/json")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("{\"json\": true}")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("{}")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("[)")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("[1,2,3]")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("\"string\"")), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<json>false</json>")), "application/xml"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<json>false</json>")), "application/xml"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("this is not json")), "text/plain"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<html><body>this is also not json</body></html>")), "text/html"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("{\"json\": true}".toByteArray())), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("{}".toByteArray())), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("[)".toByteArray())), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("[1,2,3]".toByteArray())), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("\"string\"".toByteArray())), "application/json"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<json>false</json>".toByteArray())), "application/xml"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<json>false</json>".toByteArray())), "application/xml"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("this is not json".toByteArray())), "application/octet-stream"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("<html><body>this is also not json</body></html>".toByteArray())), "text/html"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body("")), "text/plain"),
                row(Request("Get", "", emptyMap(), emptyMap(), OptionalBody.body(ByteArray(2))), "application/octet-stream"),
            )
        ) { request, mimeType ->
            request.mimeType() shouldBe mimeType
        }
    }

    "Pact charset" {

        forAll(
            table(
                headers("request", "charset"),
                row(Request("Get", ""), Consts.ISO_8859_1),
                row(Request("Get", "", emptyMap(), mapOf("Content-Type" to "text/html")), Consts.ISO_8859_1),
                row(Request("Get", "", emptyMap(), mapOf("Content-Type" to "application/json; charset=UTF-8")), Consts.UTF_8)
            )
        ) { request, charset ->
            request.charset() shouldBe charset
        }
    }
})