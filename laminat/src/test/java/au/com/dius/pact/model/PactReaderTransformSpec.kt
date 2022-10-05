package au.com.dius.pact.model

import au.com.dius.pact.PactParsingUtil
import au.com.dius.pact.external.json.Json
import com.google.gson.stream.JsonWriter
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.StringWriter

class PactReaderTransformSpec : StringSpec() {

    private val provider = mapOf("name" to "Alice Service")
    private val consumer = mapOf("name" to "Consumer")
    private val jsonMap = PactParsingUtil.parseAssetToJson("pact.json")
    private val request = mapOf(
        "method" to "GET",
        "path" to "/mallory",
        "query" to "name=ron&status=good",
        "body" to mapOf(
            "id" to "123", "method" to "create"
        )
    )
    private val response = mapOf(
        "status" to 200,
        "headers" to mapOf(
            "Content-Type" to "text/html"
        ),
        "body" to "\"That is some good Mallory.\""
    )

    init {
        "only transforms legacy fields" {
            // when:
            val result = PactReader.transformJson(jsonMap)

            result.prettySerialize() shouldBe """|{
            |  "provider": {
            |    "name": "Alice Service"
            |  },
            |  "consumer": {
            |    "name": "Consumer"
            |  },
            |  "interactions": [
            |    {
            |      "description": "a retrieve Mallory request",
            |      "request": {
            |        "method": "GET",
            |        "path": "/mallory",
            |        "query": "name=ron&status=good",
            |        "body": {
            |          "id": "123",
            |          "method": "create"
            |        }
            |      },
            |      "response": {
            |        "status": 200,
            |        "headers": {
            |          "Content-Type": "text/html"
            |        },
            |        "body": "\"That is some good Mallory.\""
            |      }
            |    }
            |  ]
            |}""".trimToAssertion()
        }

        "handles both a snake and camel case provider state" {
            // given:
            (jsonMap["interactions"][0] as Json.Object)["provider_state"] = Json.wrapInJson("provider state")
            (jsonMap["interactions"][0] as Json.Object)["providerState"] = Json.wrapInJson("provider state 2")

            // when:
            val result = PactReader.transformJson(jsonMap)

            // then:
            result.prettySerialize() shouldBe """|{
            |  "provider": {
            |    "name": "Alice Service"
            |  },
            |  "consumer": {
            |    "name": "Consumer"
            |  },
            |  "interactions": [
            |    {
            |      "description": "a retrieve Mallory request",
            |      "request": {
            |        "method": "GET",
            |        "path": "/mallory",
            |        "query": "name=ron&status=good",
            |        "body": {
            |          "id": "123",
            |          "method": "create"
            |        }
            |      },
            |      "response": {
            |        "status": 200,
            |        "headers": {
            |          "Content-Type": "text/html"
            |        },
            |        "body": "\"That is some good Mallory.\""
            |      },
            |      "providerState": "provider state 2"
            |    }
            |  ]
            |}""".trimToAssertion()
        }

        "converts request and response matching rules" {
            // given:
            (jsonMap["interactions"][0]["request"] as Json.Object)["requestMatchingRules"] =
                Json.Object("body" to Json.Object("$" to Json.Array(Json.Object("match" to Json.wrapInJson("type")))))
            (jsonMap["interactions"][0]["response"] as Json.Object)["responseMatchingRules"] =
                Json.Object("body" to Json.Object("$" to Json.Array(Json.Object("match" to Json.wrapInJson("type")))))

            // when:
            val result = PactReader.transformJson(jsonMap)

            // then:
            result.prettySerialize() shouldBe """|{
            |  "provider": {
            |    "name": "Alice Service"
            |  },
            |  "consumer": {
            |    "name": "Consumer"
            |  },
            |  "interactions": [
            |    {
            |      "description": "a retrieve Mallory request",
            |      "request": {
            |        "method": "GET",
            |        "path": "/mallory",
            |        "query": "name=ron&status=good",
            |        "body": {
            |          "id": "123",
            |          "method": "create"
            |        },
            |        "matchingRules": {
            |          "body": {
            |            "$": [
            |              {
            |                "match": "type"
            |              }
            |            ]
            |          }
            |        }
            |      },
            |      "response": {
            |        "status": 200,
            |        "headers": {
            |          "Content-Type": "text/html"
            |        },
            |        "body": "\"That is some good Mallory.\"",
            |        "matchingRules": {
            |          "body": {
            |            "$": [
            |              {
            |                "match": "type"
            |              }
            |            ]
            |          }
            |        }
            |      }
            |    }
            |  ]
            |}""".trimToAssertion()
        }

        "converts the http methods to upper case" {
            // given:
            (jsonMap["interactions"][0]["request"] as Json.Object)["method"] = Json.wrapInJson("post")

            // when:
            val result = PactReader.transformJson(jsonMap)

            // then:
            result.prettySerialize() shouldBe """|{
            |  "provider": {
            |    "name": "Alice Service"
            |  },
            |  "consumer": {
            |    "name": "Consumer"
            |  },
            |  "interactions": [
            |    {
            |      "description": "a retrieve Mallory request",
            |      "request": {
            |        "method": "POST",
            |        "path": "/mallory",
            |        "query": "name=ron&status=good",
            |        "body": {
            |          "id": "123",
            |          "method": "create"
            |        }
            |      },
            |      "response": {
            |        "status": 200,
            |        "headers": {
            |          "Content-Type": "text/html"
            |        },
            |        "body": "\"That is some good Mallory.\""
            |      }
            |    }
            |  ]
            |}""".trimToAssertion()
        }
    }

    private fun Json.prettySerialize(): String {
        val stringWriter = StringWriter()
        val gsonWriter = JsonWriter(stringWriter)
        gsonWriter.setIndent("  ")
        this.serialize(gsonWriter)
        return stringWriter.toString()
    }

    private fun String.trimToAssertion(): String {
        return this.lines().joinToString("\n") { it.trimStart().drop(1) }
    }
}