package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.PactSpecVersion

/**
 * Matching rules category
 */
data class Category @JvmOverloads constructor(
    val name: String,
    var matchingRules: MutableMap<String, MatchingRuleGroup> =
        mutableMapOf()
) {

    fun addRule(item: String, matchingRule: MatchingRule) {
        if (!matchingRules.containsKey(item)) {
            matchingRules[item] = MatchingRuleGroup(mutableListOf(matchingRule))
        } else {
            matchingRules[item]!!.rules.add(matchingRule)
        }
    }

    fun addRule(matchingRule: MatchingRule) = addRule("", matchingRule)

    fun setRule(item: String, matchingRule: MatchingRule) {
        matchingRules[item] = MatchingRuleGroup(mutableListOf(matchingRule))
    }

    fun setRule(matchingRule: MatchingRule) = setRule("", matchingRule)

    fun setRules(item: String, rules: List<MatchingRule>) {
        setRules(item, MatchingRuleGroup(rules.toMutableList()))
    }

    fun setRules(matchingRules: List<MatchingRule>) = setRules("", matchingRules)

    fun setRules(item: String, rules: MatchingRuleGroup) {
        matchingRules[item] = rules
    }

    /**
     * If the rules are empty
     */
    fun isEmpty() = matchingRules.isEmpty() || matchingRules.all { it.value.rules.isEmpty() }

    /**
     * If the rules are not empty
     */
    fun isNotEmpty() = matchingRules.any { it.value.rules.isNotEmpty() }

    fun filter(predicate: (String) -> Boolean) =
        copy(matchingRules = matchingRules.filter { predicate.invoke(it.key) }.toMutableMap())

    fun maxBy(fn: (String) -> Int): MatchingRuleGroup {
        val max = matchingRules.maxBy { fn.invoke(it.key) }
        return max?.value ?: MatchingRuleGroup()
    }

    fun allMatchingRules() = matchingRules.flatMap { it.value.rules }

    fun addRules(item: String, rules: List<MatchingRule>) {
        if (!matchingRules.containsKey(item)) {
            matchingRules[item] = MatchingRuleGroup(rules.toMutableList())
        } else {
            matchingRules[item]!!.rules.addAll(rules)
        }
    }

    fun applyMatcherRootPrefix(prefix: String) {
        matchingRules = matchingRules.mapKeys { e ->
            if (e.key.startsWith(prefix)) {
                e.key
            } else {
                prefix + e.key
            }
        }.toMutableMap()
    }

    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any?> {
        return if (pactSpecVersion < PactSpecVersion.V3) {
            matchingRules.entries.associate {
                val keyBase = "\$.$name"
                if (it.key.startsWith('$')) {
                    Pair(keyBase + it.key.substring(1), it.value.toMap(pactSpecVersion))
                } else {
                    Pair(keyBase + it.key, it.value.toMap(pactSpecVersion))
                }
            }
        } else {
            matchingRules.entries.associate { Pair(it.key, it.value.toMap(pactSpecVersion)) }
        }
    }
}