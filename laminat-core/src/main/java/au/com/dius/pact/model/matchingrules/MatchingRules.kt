package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion

data class MatchingRules(
    private val rules: MutableMap<String, Category> = HashMap()
) {

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

    fun toMap(serializationConfig: PactSerializationConfig): Map<String, Any?> {
        return if (serializationConfig.specVersion < PactSpecVersion.V3) {
            toV2Map(serializationConfig)
        } else {
            toV3Map(serializationConfig)
        }
    }

    private fun toV3Map(serializationConfig: PactSerializationConfig): Map<String, Map<String, Any?>> {
        return rules
            .filterNot { it.value.isEmpty() }
            .mapValuesTo(HashMap()) {
                it.value.toMap(serializationConfig)
            }
    }

    private fun toV2Map(serializationConfig: PactSerializationConfig): Map<String, Any?> {
        val map = HashMap<String, Any?>()

        rules.forEach { entry ->
            entry.value.toMap(serializationConfig).forEach {
                map[it.key] = it.value
            }
        }

        return map
    }

    fun getCategory(category: String): Category? {
        return rules[category]
    }

    override fun equals(other: Any?): Boolean {
        return other is MatchingRules && rules == other.rules
    }

    override fun hashCode(): Int {
        return rules.hashCode()
    }
}