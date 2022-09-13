package au.com.dius.pact.model

import au.com.dius.pact.model.serialization.SerializationConstants

class RequestResponsePact(
    override val provider: Provider,
    override val consumer: Consumer,
    var requestResponseInteractions: List<RequestResponseInteraction>
) : BasePact() {

    override val interactions: List<Interaction>
        get() = requestResponseInteractions

    override fun sortInteractions(): Pact {
        requestResponseInteractions = requestResponseInteractions.map { interaction ->
            Pair(interaction.providerStates.map { it.name }.sorted().joinToString() + interaction.description, interaction)
        }.sortedBy { it.first }
            .map { it.second }
        return this
    }

    override fun toMap(serializationConfig: PactSerializationConfig): Map<String, *> {
        return mapOf<String, Any?>(
            Pair(SerializationConstants.PROVIDER_KEY, provider.toMap()),
            Pair(SerializationConstants.CONSUMER_KEY, consumer.toMap()),
            Pair(SerializationConstants.INTERACTIONS_KEY, interactions.map { it.toMap(serializationConfig) }),
            Pair(SerializationConstants.METADATA_KEY, getMetaData(serializationConfig))
        )
    }

    override fun mergeInteractions(interactions: List<Interaction>) {
        if(interactions.any { it !is RequestResponseInteraction }) {
            throw IllegalArgumentException("Can only merge RequestResponseInteraction into a RequestResponsePact!")
        }
        requestResponseInteractions = ArrayList(requestResponseInteractions).apply {
            @Suppress("UNCHECKED_CAST")
            addAll(interactions as List<RequestResponseInteraction>)
        }.distinctBy { it.uniqueKey() }
    }

    @Deprecated(
        "Wrap the au.com.dius.pact in a FilteredPact instead",
        ReplaceWith(
            "FilteredPact(this, predicate)",
            "au.com.dius.pact.model.FilteredPact"
        )
    )
    override fun filterInteractions(predicate: (Interaction) -> Boolean): Pact {
        return FilteredPact(this, predicate)
    }

    override fun conflictsWith(other: Pact): List<Pair<Interaction, Interaction>> {
        return interactions.multiply(other.interactions)
            .filter { it.first.conflictsWith(it.second) }
    }

    override fun conflictsWithSelf(): List<Pair<Interaction, Interaction>> {
        return interactions.multiply(interactions)
            .filter { it.first !== it.second && it.first.conflictsWith(it.second) }
    }

    override fun compatibleTo(other: Pact): Boolean {
        return provider == other.provider && other is RequestResponsePact
    }

    private fun <S, T> List<S>.multiply(other: List<T>): List<Pair<S, T>> {
        return flatMap { first ->
            other.map { second -> Pair(first, second) }
        }
    }
}