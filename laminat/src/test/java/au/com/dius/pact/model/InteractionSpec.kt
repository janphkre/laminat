package au.com.dius.pact.model

import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

class InteractionSpec : StringSpec() {

    private val request: Request = Request("GET", "/")
    private val request2: Request = Request("POST", "/")
    private val response: Response = Response(200)
    private val state: ProviderState = ProviderState("state")

    init {
        "display state should show a description of the state" {
            forAll(
                table(
                    headers("state", "description"),
                    row(ProviderState("some state"), "some state"),
                    row(ProviderState(""), "None"),
                    row(ProviderState(null), "None")

                )
            ) { state, description ->
                RequestResponseInteraction("", listOf(state), request, response).displayState() shouldBe description
            }
        }

        // Different from pact-jvm 3.6.0
        "interactions do not conflict if their requests are different" {
            // given:
            val one = RequestResponseInteraction("One", listOf(state), request, response)
            val two = RequestResponseInteraction("One", listOf(state), request2, response)

            // expect:
            one.conflictsWith(two) shouldBe false
        }

        // Different from pact-jvm 3.6.0
        "interactions do conflict if their responses are different" {
            // given:
            val one = RequestResponseInteraction("One", listOf(state), request, response)
            val two = RequestResponseInteraction("One", listOf(state), request, Response(400))

            // expect:
            one.conflictsWith(two) shouldBe true
        }

        // Different from pact-jvm 3.6.0
        "interactions do conflict if they are equal" {
            // given:
            val one = RequestResponseInteraction("One", listOf(state), request, response)
            val two = RequestResponseInteraction("One", listOf(state), request, response)

            // expect:
            one.conflictsWith(two) shouldBe true
        }
    }
}