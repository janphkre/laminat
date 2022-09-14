package au.com.dius.pact.model

import au.com.dius.pact.StringSpecExt
import au.com.dius.pact.external.features.Feature
import au.com.dius.pact.external.features.FeatureFlags
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table

class PactMergeSpec : StringSpecExt() {

    private val consumer = Consumer("test_consumer")
    private val consumer2 = Consumer("other consumer")
    private val provider = Provider("test_provider")
    private val provider2 = Provider("other provider")
    private val response = Response(200, mapOf("testreqheader" to "testreqheaderval"), OptionalBody.body("{\"responsetest\":true}"))
    private val request = Request(
        "Get", "/", PactReader.queryStringToMap("q=p&q=p2&r=s"),
        mapOf("testreqheader" to "testreqheadervalue"), OptionalBody.body("{\"test\":true}")
    )
    private val interaction = RequestResponseInteraction(
        "test interaction",
        listOf(ProviderState("test state")), request, response
    )
    private val pact = RequestResponsePact(provider, consumer, listOf(interaction))

    private val anonymousPact = object : Pact {
        override val provider: Provider = provider2
        override val consumer: Consumer
            get() = throw NotImplementedError()
        override val interactions: List<Interaction>
            get() = throw NotImplementedError()
        override val source: PactSource
            get() = throw NotImplementedError()

        override fun sortInteractions(): Pact {
            throw NotImplementedError()
        }

        override fun toMap(serializationConfig: PactSerializationConfig): Map<String, *> {
            throw NotImplementedError()
        }

        override fun compatibleTo(other: Pact): Boolean {
            throw NotImplementedError()
        }

        override fun conflictsWith(other: Pact): List<Pair<Interaction, Interaction>> {
            throw NotImplementedError()
        }

        override fun conflictsWithSelf(): List<Pair<Interaction, Interaction>> {
            throw NotImplementedError()
        }

        override fun mergeInteractions(interactions: List<Interaction>) {
            throw NotImplementedError()
        }

        override fun filterInteractions(predicate: (Interaction) -> Boolean): Pact {
            throw NotImplementedError()
        }
    }

    init {

        beforeTest {
            FeatureFlags.restoreDefault(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
        }

        afterSpec {
            FeatureFlags.restoreDefault(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
        }

        "Pacts with different consumers are compatible for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(RequestResponsePact::class, RequestResponsePact(provider, consumer2, emptyList()), RequestResponsePact(provider, consumer, emptyList())),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
            }
        }

        "Pacts with different providers are not compatible for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(RequestResponsePact::class, RequestResponsePact(provider2, consumer, emptyList()), RequestResponsePact(provider, consumer, emptyList())),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.result shouldBe null
            }
        }

        "Pacts with different types are not compatible" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("newPact", "existingPact"),
                    row(RequestResponsePact(provider2, consumer, emptyList()), anonymousPact),
                )
            ) { newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.message shouldBe "Cannot merge pacts as they are not compatible"
                result.result shouldBe null
            }
        }

        "two empty compatible pacts merge ok for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(RequestResponsePact::class, RequestResponsePact(provider, consumer, emptyList()), RequestResponsePact(provider, consumer, emptyList())),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
            }
        }

        "empty pact merges with any compatible pact for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(provider, consumer, emptyList()),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
            }
        }

        "any compatible pact merges with an empty pact for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(provider, consumer, emptyList())
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
            }
        }

        "two compatible pacts merge if their interactions are compatible for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test2")), Request("POST"), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
            }
        }

        "two compatible pacts do not merge if their interactions have conflicts for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request("POST"), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.result shouldBe null
            }
        }

        // Different from pact-jvm 3.6.0: Duplicates cause error by default.
        "pact merge does not remove duplicates for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.result shouldBe null
            }
        }

        "pact merge removes exact duplicates for #type" {
            FeatureFlags.enableFeature(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe true
                result.result shouldNotBe null
                result.result!!.interactions.size shouldBe 1
            }
        }

        "pact merge fails for exact duplicates with different descriptions for #type" {
            FeatureFlags.enableFeature(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test2", listOf(ProviderState("test")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.result shouldBe null
            }
        }

        "pact merge removing exact duplicates does not remove different interactions for #type" {
            FeatureFlags.enableFeature(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test2", listOf(ProviderState("test")), Request(path = "example2"), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null

                result.result!!.interactions.size shouldBe 2
            }
        }

        "pact merge removing exact duplicates fails for different interactions for #type" {
            FeatureFlags.enableFeature(Feature.MERGE_REMOVE_EXACT_DUPLICATES)
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response())
                            )
                        ),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test", listOf(ProviderState("test")), Request(), Response(status = 123))
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.ok shouldBe false
                result.result shouldBe null
            }
        }

        "Pact merge should allow different states for #type" {
            // TODO: ADD MESSAGE PACTs once implemented
            forAll(
                table(
                    headers("type", "newPact", "existingPact"),
                    row(
                        RequestResponsePact::class,
                        RequestResponsePact(provider, consumer, listOf(interaction)),
                        RequestResponsePact(
                            provider, consumer,
                            listOf(
                                RequestResponseInteraction("test interaction", listOf(ProviderState("test state 2")), Request(), Response())
                            )
                        )
                    ),
                )
            ) { _, newPact, existingPact ->
                val result = PactMerge.merge(newPact, existingPact)
                result.message shouldBe ""
                result.ok shouldBe true
                result.result shouldNotBe null
                result.result!!.interactions shouldBe listOf(
                    RequestResponseInteraction("test interaction", listOf(ProviderState("test state 2")), Request(), Response()),
                    interaction
                )
                result.result!!.interactions.size shouldBe 2
            }
        }
    }
}