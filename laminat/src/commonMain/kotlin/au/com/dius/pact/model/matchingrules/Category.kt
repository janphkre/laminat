package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.base.PactSpecVersion
import au.com.dius.pact.util.path.PathExpression
import kotlin.jvm.JvmOverloads

/**
 * Matching rules category
 */
data class Category @JvmOverloads constructor(
    val name: String,
    var matchingRules: MutableMap<PathExpression, MatchingRuleGroup> =
        mutableMapOf()
) {

    fun addRule(item: String, matchingRule: MatchingRule) {
        val key = PathExpression(item)
        val currentValue = matchingRules[key]
        currentValue?.rules?.add(matchingRule) ?: matchingRules.put(key, MatchingRuleGroup(mutableListOf(matchingRule)))
    }

    fun addRule(matchingRule: MatchingRule) = addRule("", matchingRule)

    fun setRule(item: String, matchingRule: MatchingRule) {
        matchingRules[PathExpression(item)] = MatchingRuleGroup(mutableListOf(matchingRule))
    }

    fun setRule(matchingRule: MatchingRule) = setRule("", matchingRule)

    fun setRules(item: String, rules: List<MatchingRule>) {
        setRules(item, MatchingRuleGroup(rules.toMutableList()))
    }

    fun setRules(matchingRules: List<MatchingRule>) = setRules("", matchingRules)

    fun setRules(item: String, rules: MatchingRuleGroup) {
        matchingRules[PathExpression(item)] = rules
    }

    /**
     * If the rules are empty
     */
    fun isEmpty() = matchingRules.isEmpty() || matchingRules.all { it.value.rules.isEmpty() }

    /**
     * If the rules are not empty
     */
    fun isNotEmpty() = matchingRules.any { it.value.rules.isNotEmpty() }

    fun filter(predicate: (PathExpression) -> Boolean) =
        copy(matchingRules = matchingRules.filter { predicate.invoke(it.key) }.toMutableMap())

    fun maxBy(fn: (PathExpression) -> Int): MatchingRuleGroup {
        val max = matchingRules.maxBy { fn.invoke(it.key) }
        return max?.value ?: MatchingRuleGroup()
    }

    fun allMatchingRules() = matchingRules.flatMap { it.value.rules }

    fun addRules(item: String, rules: List<MatchingRule>) {
        val key = PathExpression(item)
        val currentValue = matchingRules[key]
        currentValue?.rules?.addAll(rules) ?: matchingRules.put(key, MatchingRuleGroup(rules.toMutableList()))
    }

    fun applyMatcherRootPrefix(prefix: String) {
        matchingRules = matchingRules.mapKeys { e ->
            if (e.key.path.startsWith(prefix)) {
                e.key
            } else {
                PathExpression(prefix + e.key)
            }
        }.toMutableMap()
    }

    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any?> {
        return if (pactSpecVersion < PactSpecVersion.V3) {
            matchingRules.entries.associate {
                val keyBase = "\$.$name"
                if (it.key.path.startsWith('$')) {
                    Pair(keyBase + it.key.path.substring(1), it.value.toMap(pactSpecVersion))
                } else {
                    Pair(keyBase + it.key, it.value.toMap(pactSpecVersion))
                }
            }
        } else {
            matchingRules.entries.associate { Pair(it.key.path, it.value.toMap(pactSpecVersion)) }
        }
    }
}