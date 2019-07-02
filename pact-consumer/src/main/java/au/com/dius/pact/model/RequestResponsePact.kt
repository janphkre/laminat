package au.com.dius.pact.model

import org.apache.commons.collections4.Predicate

class RequestResponsePact(override val provider: Provider, override val consumer: Consumer, var requestResponseInteractions: List<RequestResponseInteraction>) : BasePact() {

    override val interactions: List<Interaction>
    get() = requestResponseInteractions

    override fun sortInteractions(): Pact {
        requestResponseInteractions = ArrayList<RequestResponseInteraction>(requestResponseInteractions).sortedBy {
            it.providerState + it.description
        }
        return this
    }

    override fun toMap(pactSpecVersion: PactSpecVersion): Map<String, *> {
        return mapOf<String, Any?>(
            Pair("provider", provider.toMap()),
            Pair("consumer", consumer.toMap()),
            Pair("interactions", interactions.map { it.toMap(pactSpecVersion) }),
            Pair("metadata", getMetaData(if (pactSpecVersion >= PactSpecVersion.V3) "3.0.0" else "2.0.0"))
        )
    }

    override fun mergeInteractions(interactions: List<Interaction>) {
        requestResponseInteractions = ArrayList<RequestResponseInteraction>(requestResponseInteractions).apply {
            addAll(interactions as List<RequestResponseInteraction>)
        }.distinctBy { it.uniqueKey() }
    }

    override fun filterInteractions(predicate: Predicate<Interaction>): Pact {
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

    fun <S, T> List<S>.multiply(other: List<T>): List<Pair<S, T>> {
        return flatMap { first ->
            other.map { second -> Pair(first, second) }
        }
    }
}