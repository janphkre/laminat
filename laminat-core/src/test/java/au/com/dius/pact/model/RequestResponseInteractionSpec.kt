package au.com.dius.pact.model

import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.generators.RandomStringGenerator
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class RequestResponseInteractionSpec : StringSpec() {

    private val generators = Generators(mutableMapOf(Category.HEADER to mutableMapOf("a" to RandomStringGenerator(4))))

    private val interaction = RequestResponseInteraction(
        "test interaction",
        listOf(
            ProviderState("state one"),
            ProviderState("state two", mapOf("value" to "one", "other" to "2"))
        ),
        Request(generators = generators),
        Response(generators = generators)
    )

    init {
        "creates a V3 map format if V3 spec" {
            // when:
            val map = interaction.toMap(PactSerializationConfig(PactSpecVersion.V3, null))

            // then:
            map shouldBe mapOf(
                "description" to "test interaction",
                "request" to mapOf("method" to "GET", "path" to "/", "generators" to mapOf("header" to mapOf("a" to mapOf("type" to "RandomString", "size" to 4)))),
                "response" to mapOf("status" to 200, "generators" to mapOf("header" to mapOf("a" to mapOf("type" to "RandomString", "size" to 4)))),
                "providerStates" to listOf(
                    mapOf("name" to "state one"),
                    mapOf("name" to "state two", "params" to mapOf("value" to "one", "other" to "2"))
                )
            )
        }

        "creates a V2 map format if not V3 spec" {
            val map = interaction.toMap(PactSerializationConfig(PactSpecVersion.V2, null))

            // then:
            map shouldBe mapOf(
                "description" to "test interaction",
                "request" to mapOf("method" to "GET", "path" to "/"),
                "response" to mapOf("status" to 200),
                "providerState" to "state one"
            )
        }

        "does not include a provide state if there is not any" {
            // when:
            val emptyInteraction = RequestResponseInteraction(
                "test interaction",
                emptyList(),
                Request(generators = generators),
                Response(generators = generators)
            )
            val mapV3 = emptyInteraction.toMap(PactSerializationConfig(PactSpecVersion.V3, null))
            val mapV2 = emptyInteraction.toMap(PactSerializationConfig(PactSpecVersion.V2, null))

            // then:
            mapV3.containsKey("providerStates") shouldBe false
            mapV3.containsKey("providerState") shouldBe false
            mapV2.containsKey("providerStates") shouldBe false
            mapV2.containsKey("providerState") shouldBe false
        }

        "unique key test" {
            // where:
            val interaction1 = RequestResponseInteraction("description 1+2", emptyList(), Request(), Response())
            val interaction2 = RequestResponseInteraction("description 1+2", emptyList(), Request(), Response())
            val interaction3 = RequestResponseInteraction("description 1+2", listOf(ProviderState("state 3")), Request(), Response())
            val interaction4 = RequestResponseInteraction("description 4", emptyList(), Request(), Response())
            val interaction5 = RequestResponseInteraction("description 4", listOf(ProviderState("state 5")), Request(), Response())

            // expect:
            interaction1.uniqueKey() shouldBe interaction1.uniqueKey()
            interaction1.uniqueKey() shouldBe interaction2.uniqueKey()
            interaction1.uniqueKey() shouldNotBe interaction3.uniqueKey()
            interaction1.uniqueKey() shouldNotBe interaction4.uniqueKey()
            interaction1.uniqueKey() shouldNotBe interaction5.uniqueKey()
            interaction3.uniqueKey() shouldNotBe interaction4.uniqueKey()
            interaction3.uniqueKey() shouldNotBe interaction5.uniqueKey()
            interaction4.uniqueKey() shouldNotBe interaction5.uniqueKey()
        }

        "displayState test" {
            forAll(
                table(
                    headers("providerStates", "stateDescription"),
                    row(emptyList(), "None"),
                    row(listOf(ProviderState(null)), "None"),
                    row(listOf(ProviderState("state 1")), "state 1"),
                    row(listOf(ProviderState("state 1"), ProviderState("state 2")), "state 1, state 2"),
                )
            ) { providerStates, stateDescription ->
                val testInteraction = RequestResponseInteraction("test", providerStates, Request(), Response())
                testInteraction.displayState() shouldBe stateDescription
            }
        }
    }
}