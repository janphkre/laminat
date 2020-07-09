package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.base.PactSpecVersion

class MatchingRules {

    private val rules = HashMap<String, Category>()

    fun addCategory(category: String): Category {
        if (!rules.containsKey(category)) {
            rules[category] = Category(category)
        }
        return rules[category]!!
    }

    fun addCategory(category: Category): Category {
        rules[category.name] = category
        return category
    }

    /**
     * If the rules are empty
     */
    fun isEmpty(): Boolean {
        return rules.all { it.value.isEmpty() }
    }

    /**
     * If the rules are not empty
     */
    fun isNotEmpty(): Boolean {
        return rules.any { it.value.isNotEmpty() }
    }

    fun hasCategory(category: String): Boolean {
        return rules.containsKey(category)
    }

    fun getCategories(): Set<String> {
        return rules.keys
    }

    override fun toString(): String {
        return "MatchingRules(rules=$rules)"
    }

    fun copy(): MatchingRules {
        val matchingRules = MatchingRules()

        rules.forEach {
            matchingRules.addCategory(it.value /*.copy()*/)
        }

        return matchingRules
    }

    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any?> {
        return if (pactSpecVersion < PactSpecVersion.V3) {
            toV2Map()
        } else {
            toV3Map()
        }
    }

    fun toV3Map(): Map<String, Map<String, Any?>> {
        val map = HashMap<String, Map<String, Any?>>()

        rules.forEach {
            map[it.key] = it.value.toMap(PactSpecVersion.V3)
        }

        return map
    }

    fun toV2Map(): Map<String, Any?> {
        val map = HashMap<String, Any?>()

        rules.forEach {
            it.value.toMap(PactSpecVersion.V2).forEach {
                map[it.key] = it.value
            }
        }

        return map
    }

    fun getCategory(category: String): Category? {
        return rules.get(category)
    }
}