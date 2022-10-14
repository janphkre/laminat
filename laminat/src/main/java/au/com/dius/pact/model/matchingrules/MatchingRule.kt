package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.serialization.SerializationConstants

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
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.DATE.type,
        SerializationConstants.DATE_KEY to format
    )
}

/**
 * Matching rule for equality
 */
object EqualsMatcher : MatchingRule {
    override fun toMap() = mapOf(SerializationConstants.MATCH_KEY to MatchingRulesSerialization.EQUALS.type)
}

/**
 * Matcher for a substring in a string
 */
data class IncludeMatcher(val value: String) : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.INCLUDE.type,
        SerializationConstants.VALUE_KEY to value
    )
}

/**
 * Type matching with a maximum size
 */
data class MaxTypeMatcher(val max: Int) : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TYPE.type,
        SerializationConstants.MAX_KEY to max
    )
}

/**
 * Type matcher with a minimum size and maximum size
 */
data class MinMaxTypeMatcher(val min: Int, val max: Int) : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TYPE.type,
        SerializationConstants.MIN_KEY to min,
        SerializationConstants.MAX_KEY to max
    )
}

/**
 * Type matcher with a minimum size
 */
data class MinTypeMatcher(val min: Int) : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TYPE.type,
        SerializationConstants.MIN_KEY to min
    )
}

/**
 * Type matching for numbers
 */
data class NumberTypeMatcher(val numberType: NumberType) : MatchingRule {
    enum class NumberType(val serialization: MatchingRulesSerialization) {
        NUMBER(MatchingRulesSerialization.NUMBER),
        INTEGER(MatchingRulesSerialization.INTEGER),
        DECIMAL(MatchingRulesSerialization.DECIMAL)
    }

    override fun toMap() = mapOf(SerializationConstants.MATCH_KEY to numberType.serialization.type)
}

/**
 * Regular Expression Matcher
 */
data class RegexMatcher @JvmOverloads constructor(val regex: Regex, val example: String? = null) : MatchingRule {

    override fun equals(other: Any?): Boolean {
        if (other !is RegexMatcher) {
            return false
        }
        return example == other.example &&
            regex.toString() == other.regex.toString()
    }

    @JvmOverloads
    constructor(regex: String, example: String? = null) : this(Regex(regex), example)

    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.REGEX.type,
        SerializationConstants.REGEX_KEY to regex.toString()
    )
}

/**
 * Matcher for time values
 */
data class TimeMatcher @JvmOverloads constructor(val format: String = "HH:mm:ss") : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TIME.type,
        SerializationConstants.TIME_KEY to format
    )
}

/**
 * Matcher for time values
 */
data class TimestampMatcher @JvmOverloads constructor(val format: String = "yyyy-MM-dd HH:mm:ssZZZ") : MatchingRule {
    override fun toMap() = mapOf(
        SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TIMESTAMP.type,
        SerializationConstants.TIMESTAMP_KEY to format
    )
}

/**
 * Matcher for types
 */
object TypeMatcher : MatchingRule {
    override fun toMap() = mapOf(SerializationConstants.MATCH_KEY to MatchingRulesSerialization.TYPE.type)
}

/**
 * Matcher for null values
 */
object NullMatcher : MatchingRule {
    override fun toMap() = mapOf(SerializationConstants.MATCH_KEY to MatchingRulesSerialization.NULL.type)
}

object ValuesMatcher: MatchingRule {
    override fun toMap(): Map<String, Any?> = mapOf(SerializationConstants.MATCH_KEY to MatchingRulesSerialization.VALUES.type)
}

data class MatchingRuleGroup @JvmOverloads constructor(
    val rules: MutableList<MatchingRule> = mutableListOf(),
    val ruleLogic: RuleLogic = RuleLogic.AND
) {
    fun toMap(serializationConfig: PactSerializationConfig): Map<String, Any?> {
        return if (serializationConfig.specVersion < PactSpecVersion.V3) {
            rules.first().toMap()
        } else {
            mapOf(
                SerializationConstants.MATCHERS_KEY to rules.map { it.toMap() },
                SerializationConstants.COMBINE_KEY to ruleLogic.name
            )
        }
    }
}