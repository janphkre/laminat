package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.base.PactSpecVersion
import kotlin.jvm.JvmOverloads

/**
 * Logic to use to combine rules
 */
enum class RuleLogic {
    AND, OR
}

/**
 * Matching rule
 */
interface MatchingRule {
    fun toMap(): Map<String, Any?>
}

/**
 * Matching Rule for dates
 */
data class DateMatcher @JvmOverloads constructor(val format: String = "yyyy-MM-dd") : MatchingRule {
    override fun toMap() = mapOf("match" to "date", "date" to format)
}

/**
 * Matching rule for equality
 */
object EqualsMatcher : MatchingRule {
    override fun toMap() = mapOf("match" to "equality")
}

/**
 * Matcher for a substring in a string
 */
data class IncludeMatcher(val value: String) : MatchingRule {
    override fun toMap() = mapOf("match" to "include", "value" to value)
}

/**
 * Type matching with a maximum size
 */
data class MaxTypeMatcher(val max: Int) : MatchingRule {
    override fun toMap() = mapOf("match" to "type", "max" to max)
}

/**
 * Type matcher with a minimum size and maximum size
 */
data class MinMaxTypeMatcher(val min: Int, val max: Int) : MatchingRule {
    override fun toMap() = mapOf("match" to "type", "min" to min, "max" to max)
}

/**
 * Type matcher with a minimum size
 */
data class MinTypeMatcher(val min: Int) : MatchingRule {
    override fun toMap() = mapOf("match" to "type", "min" to min)
}

/**
 * Type matching for numbers
 */
data class NumberTypeMatcher(val numberType: NumberType) : MatchingRule {
    enum class NumberType {
        NUMBER,
        INTEGER,
        DECIMAL
    }

    override fun toMap() = mapOf("match" to numberType.name.toLowerCase())
}

/**
 * Regular Expression Matcher
 */
data class RegexMatcher @JvmOverloads constructor(val regex: Regex, val example: String? = null) : MatchingRule {

    @JvmOverloads
    constructor(regex: String, example: String? = null) : this(Regex(regex), example)

    override fun toMap() = mapOf("match" to "regex", "regex" to regex.toString())
}

/**
 * Matcher for time values
 */
data class TimeMatcher @JvmOverloads constructor(val format: String = "HH:mm:ss") : MatchingRule {
    override fun toMap() = mapOf("match" to "time", "time" to format)
}

/**
 * Matcher for time values
 */
data class TimestampMatcher @JvmOverloads constructor(val format: String = "yyyy-MM-dd HH:mm:ssZZZ") : MatchingRule {
    override fun toMap() = mapOf("match" to "timestamp", "timestamp" to format)
}

/**
 * Matcher for types
 */
object TypeMatcher : MatchingRule {
    override fun toMap() = mapOf("match" to "type")
}

/**
 * Matcher for null values
 */
object NullMatcher : MatchingRule {
    override fun toMap() = mapOf("match" to "null")
}

data class MatchingRuleGroup @JvmOverloads constructor(
    val rules: MutableList<MatchingRule> = mutableListOf(),
    val ruleLogic: RuleLogic = RuleLogic.AND
) {
    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any?> {
        if (pactSpecVersion < PactSpecVersion.V3) {
            return rules.first().toMap()
        } else {
            return mapOf("matchers" to rules.map { it.toMap() }, "combine" to ruleLogic.name)
        }
    }
}