package au.com.dius.pact.model

class FilteredPact(val pact: Pact, private val interactionPredicate: (Interaction) -> Boolean) : Pact by pact {
    override val interactions: List<Interaction>
        get() = pact.interactions.filter { interactionPredicate.invoke(it) }

    fun isNotFiltered() = pact.interactions.all { interactionPredicate.invoke(it) }

    fun isFiltered() = pact.interactions.any { !interactionPredicate.invoke(it) }

    override fun toString(): String {
        return "FilteredPact(au.com.dius.pact=$pact, filtered=${isFiltered()})"
    }
}