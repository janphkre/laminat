package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.serialization.SerializationConstants

enum class MatchingRulesSerialization(
    val type: String
) {

    DATE(SerializationConstants.DATE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return DateMatcher(json[SerializationConstants.DATE_KEY].getValue())
        }
    },
    EQUALS(SerializationConstants.EQUALITY_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return EqualsMatcher
        }
    },
    INCLUDE(SerializationConstants.INCLUDE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return IncludeMatcher(json[SerializationConstants.VALUE_KEY].getValue())
        }
    },
    NUMBER(SerializationConstants.NUMBER_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return NumberTypeMatcher(NumberTypeMatcher.NumberType.NUMBER)
        }
    },
    INTEGER(SerializationConstants.INTEGER_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return NumberTypeMatcher(NumberTypeMatcher.NumberType.INTEGER)
        }
    },
    DECIMAL(SerializationConstants.DECIMAL_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL)
        }
    },
    REGEX(SerializationConstants.REGEX_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return RegexMatcher(json[SerializationConstants.REGEX_KEY].getValue<String>(), null)
        }
    },
    TIME(SerializationConstants.TIME_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return TimeMatcher(json[SerializationConstants.TIME_KEY].getValue())
        }
    },
    TIMESTAMP(SerializationConstants.TIMESTAMP_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return TimestampMatcher(json[SerializationConstants.TIMESTAMP_KEY].getValue())
        }
    },
    TYPE(SerializationConstants.TYPE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return TypeMatcher
        }
    },
    NULL(SerializationConstants.NULL_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return NullMatcher
        }
    };

    abstract fun fromJson(json: Json.Object): MatchingRule

    companion object {
        fun fromJson(json: Json): MatchingRule {
            json as Json.Object
            val typeString = json[SerializationConstants.MATCH_KEY].getValue<String>()
            val type = values().first { it.type.equals(typeString, ignoreCase = true) }
            return type.fromJson(json)
        }
    }
}