package au.com.dius.pact.matchers

import au.com.dius.pact.model.matchingrules.DateMatcher
import au.com.dius.pact.model.matchingrules.IncludeMatcher
import au.com.dius.pact.model.matchingrules.MatchingRule
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.NullMatcher
import au.com.dius.pact.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.matchingrules.RuleLogic
import au.com.dius.pact.model.matchingrules.TimeMatcher
import au.com.dius.pact.model.matchingrules.TimestampMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import com.google.gson.JsonElement
import org.apache.commons.lang3.time.DateUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.text.ParseException

fun valueOf(value: Any?): String {
    return when (value) {
        null -> "null"
        is String -> "'$value'"
        else -> value.toString()
    }
}

fun <Mismatch> matchInclude(includedValue: String, expected: Any?, actual: Any?,
                            mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    val matches = actual.toString().contains(includedValue)
    return if (matches) {
        listOf()
    } else {
        listOf(mismatchFactory.create(expected, actual,
            "Expected ${valueOf(actual)} to include ${valueOf(
                includedValue
            )}"))
    }
}

/**
 * Executor for matchers
 */
fun <Mismatch> domatch(matchers: MatchingRuleGroup, expected: Any?, actual: Any?,
                       mismatchFn: MismatchFactory<Mismatch>
): List<Mismatch> {
    val result = matchers.rules.map { matchingRule ->
        domatch(matchingRule, expected, actual, mismatchFn)
    }

    return if (matchers.ruleLogic == RuleLogic.AND) {
        result.flatten()
    } else {
        if (result.any { it.isEmpty() }) {
            emptyList()
        } else {
            result.flatten()
        }
    }
}

fun <Mismatch> domatch(matcher: MatchingRule, expected: Any?, actual: Any?,
                       mismatchFn: MismatchFactory<Mismatch>
): List<Mismatch> {
    return when (matcher) {
        is RegexMatcher -> matchRegex(matcher.regex, expected, actual, mismatchFn)
        is TypeMatcher -> matchType(expected, actual, mismatchFn)
        is NumberTypeMatcher -> matchNumber(matcher.numberType, expected, actual, mismatchFn)
        is DateMatcher -> matchDate(matcher.format, expected, actual, mismatchFn)
        is TimeMatcher -> matchTime(matcher.format, expected, actual, mismatchFn)
        is TimestampMatcher -> matchTimestamp(matcher.format, expected, actual, mismatchFn)
        is MinTypeMatcher -> matchMinType(matcher.min, expected, actual, mismatchFn)
        is MaxTypeMatcher -> matchMaxType(matcher.max, expected, actual, mismatchFn)
        is IncludeMatcher -> matchInclude(matcher.value, expected, actual, mismatchFn)
        is NullMatcher -> matchNull(actual, mismatchFn)
        else -> matchEquality(expected, actual, mismatchFn)
    }
}

fun <Mismatch> matchEquality(expected: Any?, actual: Any?,
                             mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    val matches = actual == null && expected == null || actual != null && actual == expected
    return if (matches) {
        emptyList()
    } else {
        listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to equal ${valueOf(
            actual
        )}"))
    }
}

fun <Mismatch> matchRegex(regex: Regex, expected: Any?, actual: Any?,
                          mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    val matches = actual.toString().matches(regex)
    return if (matches
        || expected is List<*> && actual is List<*>
        || expected is Map<*, *> && actual is Map<*, *>) {
        emptyList()
    } else {
        listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to match '$regex'"))
    }
}

fun <Mismatch> matchType(expected: Any?, actual: Any?,
                         mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return if (expected is String && actual is String
        || expected is Number && actual is Number
        || expected is Boolean && actual is Boolean
        || expected is List<*> && actual is List<*>
        || expected is Map<*, *> && actual is Map<*, *>) {
        emptyList()
    } else if (expected == null) {
        if (actual == null) {
            emptyList()
        } else {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to be null"))
        }
    } else {
        listOf(mismatchFactory.create(expected, actual,
            "Expected ${valueOf(actual)} to be the same type as ${valueOf(
                expected
            )}"))
    }
}

fun <Mismatch> matchNumber(numberType: NumberTypeMatcher.NumberType,  expected: Any?, actual: Any?,
                           mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    if (expected == null && actual != null) {
        return listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to be null"))
    }
    when (numberType) {
        NumberTypeMatcher.NumberType.NUMBER -> {
            if (actual !is Number) {
                return listOf(mismatchFactory.create(expected, actual,
                    "Expected ${valueOf(actual)} to be a number"))
            }
        }
        NumberTypeMatcher.NumberType.INTEGER -> {
            if (actual !is Int && actual !is Long && actual !is BigInteger) {
                return listOf(mismatchFactory.create(expected, actual,
                    "Expected ${valueOf(actual)} to be an integer"))
            }
        }
        NumberTypeMatcher.NumberType.DECIMAL -> {
            if (actual !is Float && actual !is Double && actual !is BigDecimal && actual != 0) {
                return listOf(mismatchFactory.create(expected, actual,
                    "Expected ${valueOf(actual)} to be a decimal number"))
            }
        }
    }
    return emptyList()
}

fun <Mismatch> matchDate(pattern: String, expected: Any?, actual: Any?,
                         mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return try {
        DateUtils.parseDate(actual.toString(), pattern)
        emptyList()
    } catch (e: ParseException) {
        listOf(mismatchFactory.create(expected, actual,
            "Expected ${valueOf(actual)} to match a date of '$pattern': " +
                    "${e.message}"))
    }
}

fun <Mismatch> matchTime(pattern: String, expected: Any?, actual: Any?,
                         mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return try {
        DateUtils.parseDate(actual.toString(), pattern)
        emptyList()
    } catch (e: ParseException) {
        listOf(mismatchFactory.create(expected, actual,
            "Expected ${valueOf(actual)} to match a time of '$pattern': " +
                    "${e.message}"))
    }
}

fun <Mismatch> matchTimestamp(pattern: String, expected: Any?, actual: Any?,
                              mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return try {
        DateUtils.parseDate(actual.toString(), pattern)
        emptyList()
    } catch (e: ParseException) {
        listOf(mismatchFactory.create(expected, actual,
            "Expected ${valueOf(actual)} to match a timestamp of '$pattern': " +
                    "${e.message}"))
    }
}

fun <Mismatch> matchMinType(min: Int, expected: Any?, actual: Any?,
                            mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return if (actual is List<*>) {
        if (actual.size < min) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have minimum $min"))
        } else {
            emptyList()
        }
    } else if (actual is scala.collection.immutable.List<*>) {
        if (actual.size() < min) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have minimum $min"))
        } else {
            emptyList()
        }
    } else if (actual is JsonElement) {
        if (actual.isJsonArray && actual.asJsonArray.size() < min) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have minimum $min"))
        } else {
            emptyList()
        }
    } else {
        matchType(expected, actual, mismatchFactory)
    }
}

fun <Mismatch> matchMaxType(max: Int, expected: Any?, actual: Any?,
                            mismatchFactory: MismatchFactory<Mismatch>
): List<Mismatch> {
    return if (actual is List<*>) {
        if (actual.size > max) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have maximum $max"))
        } else {
            emptyList()
        }
    } else if (actual is scala.collection.immutable.List<*>) {
        if (actual.size() > max) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have maximum $max"))
        } else {
            emptyList()
        }
    } else if (actual is JsonElement) {
        if (actual.isJsonArray && actual.asJsonArray.size() > max) {
            listOf(mismatchFactory.create(expected, actual, "Expected ${valueOf(actual)} to have maximum $max"))
        } else {
            emptyList()
        }
    } else {
        matchType(expected, actual, mismatchFactory)
    }
}

fun <Mismatch> matchNull(actual: Any?, mismatchFactory: MismatchFactory<Mismatch>): List<Mismatch> {
    val matches = actual == null
    return if (matches) {
        emptyList()
    } else {
        listOf(mismatchFactory.create(null, actual, "Expected ${valueOf(actual)} to be null"))
    }
}