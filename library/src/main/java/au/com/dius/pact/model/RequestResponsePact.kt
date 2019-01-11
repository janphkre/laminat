package au.com.dius.pact.model

import com.google.common.collect.Lists
import org.apache.commons.collections4.Predicate

class RequestResponsePact(override val provider: Provider, override val consumer: Consumer, var requestResponseInteractions: List<RequestResponseInteraction>): BasePact() {

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
            Pair("provider",provider.toMap()),
            Pair("consumer", consumer.toMap()),
            Pair("interactions", interactions.map { it.toMap(pactSpecVersion) }),
            Pair("metadata", getMetaData(if(pactSpecVersion >= PactSpecVersion.V3) "3.0.0" else "2.0.0"))
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
        return Lists.cartesianProduct(interactions, other.interactions)
            .map { it[0] to it[1] }
            .filter { it.first.conflictsWith(it.second) }
    }
}