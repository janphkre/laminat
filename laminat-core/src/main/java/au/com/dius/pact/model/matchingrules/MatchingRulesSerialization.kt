package au.com.dius.pact.model.matchingrules

import au.com.dius.pact.external.Logging
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValue
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.serialization.SerializationConstants

enum class MatchingRulesSerialization(
    val type: String
) {

    DATE(SerializationConstants.DATE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return json[SerializationConstants.DATE_KEY].getValueOrNull<String>()?.let { DateMatcher(it) } ?: DateMatcher()
        }
    },
    EQUALS(SerializationConstants.EQUALITY_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return EqualsMatcher
        }
    },
    INCLUDE(SerializationConstants.INCLUDE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            val value = json[SerializationConstants.VALUE_KEY].getValueOrNull<String>()
                ?: json[SerializationConstants.INCLUDE_KEY].getValueOrNull<String>()
                ?: throw UnsupportedOperationException("Can not create a include matcher without a value!")
            return IncludeMatcher(value)
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
    REAL(SerializationConstants.REAL_KEY) {
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
            return json[SerializationConstants.TIME_KEY].getValueOrNull<String>()?.let { TimeMatcher(it) } ?: TimeMatcher()
        }
    },
    TIMESTAMP(SerializationConstants.TIMESTAMP_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return json[SerializationConstants.TIMESTAMP_KEY].getValueOrNull<String>()?.let { TimestampMatcher(it) } ?: TimestampMatcher()
        }
    },
    TYPE(SerializationConstants.TYPE_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return if (json.containsKey(SerializationConstants.MIN_KEY) && json.containsKey(SerializationConstants.MAX_KEY)) {
                MinMaxTypeMatcher(json[SerializationConstants.MIN_KEY].getValue(), json[SerializationConstants.MAX_KEY].getValue())
            } else if (json.containsKey(SerializationConstants.MIN_KEY)) {
                return MIN.fromJson(json)
            } else if (json.containsKey(SerializationConstants.MAX_KEY)) {
                return MAX.fromJson(json)
            } else {
                TypeMatcher
            }
        }
    },
    MIN(SerializationConstants.MIN_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return MinTypeMatcher(json[SerializationConstants.MIN_KEY].getValue())
        }
    },
    MAX(SerializationConstants.MAX_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return MaxTypeMatcher(json[SerializationConstants.MAX_KEY].getValue())
        }
    },
    NULL(SerializationConstants.NULL_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return NullMatcher
        }
    },
    VALUES(SerializationConstants.VALUES_KEY) {
        override fun fromJson(json: Json.Object): MatchingRule {
            return ValuesMatcher
        }
    };

    abstract fun fromJson(json: Json.Object): MatchingRule

    companion object {
        fun fromJson(json: Json): MatchingRule {
            json as Json.Object
            val typeString = json[SerializationConstants.MATCH_KEY].getValueOrNull<String>() ?: return fromJsonGuess(json)
            val type = values().firstOrNull { it.type.equals(typeString, ignoreCase = true) }
            if (type == null) {
                Logging.warn { "Unrecognised matcher definition $json, defaulting to equality matching" }
                return EqualsMatcher
            }
            return type.fromJson(json)
        }

        private fun fromJsonGuess(json: Json.Object): MatchingRule {
            return when {
                json.containsKey(SerializationConstants.REGEX_KEY) -> REGEX.fromJson(json)
                json.containsKey(SerializationConstants.MIN_KEY) -> TYPE.fromJson(json)
                json.containsKey(SerializationConstants.MAX_KEY) -> TYPE.fromJson(json)
                json.containsKey(SerializationConstants.TIMESTAMP_KEY) -> TIMESTAMP.fromJson(json)
                json.containsKey(SerializationConstants.TIME_KEY) -> TIME.fromJson(json)
                json.containsKey(SerializationConstants.DATE_KEY) -> DATE.fromJson(json)
                else -> {
                    Logging.warn { "Unrecognised matcher definition without a key $json, defaulting to equality matching" }
                    EqualsMatcher
                }
            }
        }
    }
}