package au.com.dius.pact.model

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec

class RequestResponsePactSpec : StringSpec() {

    private val provider = Provider()
    private val consumer = Consumer()
    private val interaction = RequestResponseInteraction(
        request = Request("GET"),
        response = Response(
            body = OptionalBody.body("{\"value\": 1234.0}"),
            headers = mapOf("Content-Type" to "application/json")
        ),
        description = "",
        providerStates = emptyList()
    )

    init {
        "when writing V2 spec, query parameters must be encoded appropriately" {
            // given:
            val pact = RequestResponsePact(
                provider, consumer,
                listOf(
                    RequestResponseInteraction(
                        request = Request(method = "GET", query = mapOf("a" to listOf("b=c&d"))),
                        response = Response(),
                        description = "",
                        providerStates = emptyList()
                    )
                )
            )

            // when:
            val result = pact.toMap(PactSerializationConfig(PactSpecVersion.V2, null))

            // then:
            ((((result["interactions"] as List<*>)[0] as Map<String, *>)["request"] as Map<String, *>)["query"] as String) shouldBe "a=b%3Dc%26d"
        }

        "should handle body types other than JSON" {
            // given:
            val pact = RequestResponsePact(
                provider, consumer,
                listOf(
                    RequestResponseInteraction(
                        request = Request(
                            method = "PUT",
                            body = OptionalBody.body("<?xml version=\"1.0\"><root/>"),
                            headers = mapOf("Content-Type" to "application/xml")
                        ),
                        response = Response(body = OptionalBody.body("Ok, no prob"), headers = mapOf("Content-Type" to "text/plain")),

                        description = "",
                        providerStates = emptyList()
                    )
                )
            )

            // when:
            val result = pact.toMap(PactSerializationConfig(PactSpecVersion.V3, null))

            // then:
            ((((result["interactions"] as List<*>)[0] as Map<String, *>)["request"] as Map<String, *>)["body"] as String) shouldBe "<?xml version=\"1.0\"><root/>"
            ((((result["interactions"] as List<*>)[0] as Map<String, *>)["response"] as Map<String, *>)["body"] as String) shouldBe "Ok, no prob"
        }

        "does not lose the scale for decimal numbers" {
            // given:
            val pact = RequestResponsePact(
                provider, consumer,
                listOf(
                    RequestResponseInteraction(
                        request = Request(method = "GET"),
                        response = Response(
                            body = OptionalBody.body("{\"value\": 1234.0}"),
                            headers = mapOf("Content-Type" to "application/json")
                        ),
                        description = "",
                        providerStates = emptyList()
                    )
                )
            )

            // when:
            val result = pact.toMap(PactSerializationConfig(PactSpecVersion.V3, null))

            // then:
            ((((result["interactions"] as List<*>)[0] as Map<String, *>)["response"] as Map<String, *>)["body"].toString()) shouldBe "{\"value\":1234.0}"
        }

        "equality test" {
            // where:
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))

            // expect:
            (pact == pact) shouldBe true
        }

        "pacts are not equal if the providers are different" {
            // where:
            val provider2 = Provider("other provider")
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))
            val pact2 = RequestResponsePact(provider2, consumer, listOf(interaction))

            // expect:
            pact shouldNotBe pact2
        }

        "pacts are not equal if the consumers are different" {
            // where:
            val consumer2 = Consumer("other consumer")
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))
            val pact2 = RequestResponsePact(provider, consumer2, listOf(interaction))

            // expect:
            pact != pact2
        }

        // Left out from pact-jvm. Changing the metadata should not be supported.
        // "pacts are equal if the metadata is different" { }

        "pacts are not equal if the interactions are different" {
            // where:
            val interaction2 = RequestResponseInteraction(
                request = Request(method = "POST"),
                response = Response(
                    body = OptionalBody.body("{\"value\": 1234.0}"),
                    headers = mapOf("Content-Type" to "application/json")
                ),
                description = "",
                providerStates = emptyList()
            )
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))
            val pact2 = RequestResponsePact(provider, consumer, listOf(interaction2))

            // expect:
            pact shouldNotBe pact2
        }

        "pacts are not equal if the number of interactions are different" {
            // where:
            val interaction2 = RequestResponseInteraction(
                request = Request(method = "POST"),
                response = Response(
                    body = OptionalBody.body("{\"value\": 1234.0}"),
                    headers = mapOf("Content-Type" to "application/json")
                ),
                description = "",
                providerStates = emptyList()
            )
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))
            val pact2 = RequestResponsePact(provider, consumer, listOf(interaction, interaction2))

            // expect:
            pact != pact2
        }

        "when filtering the pact, do not loose the source of the pact" {
            // given:
            val source = PactSource.BrokerUrlSource("url", "brokerUrl")
            val pact = RequestResponsePact(provider, consumer, listOf(interaction))
            pact.source = source

            // when:
            val result = FilteredPact(pact) { true }

            // then:
            result.source shouldBe source
        }
    }
}