package au.com.dius.pact.model.generators

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValue
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.serialization.SerializationConstants

enum class GeneratorSerialization(
    val type: String
) {
    RANDOM_INT("RandomInt") {
        override fun fromJson(json: Json.Object): Generator {
            return RandomIntGenerator(
                min = json[SerializationConstants.MIN_KEY].getValue<Number>().toInt(),
                max = json[SerializationConstants.MAX_KEY].getValue<Number>().toInt()
            )
        }
    },
    RANDOM_DECIMAL("RandomDecimal") {
        override fun fromJson(json: Json.Object): Generator {
            return RandomDecimalGenerator(
                digits = json[SerializationConstants.DIGITS_KEY].getValue<Number>().toInt()
            )
        }
    },
    RANDOM_HEXADECIMAL("RandomHexadecimal") {
        override fun fromJson(json: Json.Object): Generator {
            return RandomHexadecimalGenerator(
                digits = json[SerializationConstants.DIGITS_KEY].getValue<Number>().toInt()
            )
        }
    },
    RANDOM_STRING("RandomString") {
        override fun fromJson(json: Json.Object): Generator {
            return RandomStringGenerator(
                json[SerializationConstants.SIZE_KEY].getValue()
            )
        }
    },
    REGEX("Regex") {
        override fun fromJson(json: Json.Object): Generator {
            return RegexGenerator(
                json[SerializationConstants.REGEX_KEY].getValue()
            )
        }
    },
    UUID("Uuid") {
        override fun fromJson(json: Json.Object): Generator {
            return UuidGenerator()
        }
    },
    DATE("Date") {
        override fun fromJson(json: Json.Object): Generator {
            return DateGenerator(
                json[SerializationConstants.FORMAT_KEY].getValueOrNull()
            )
        }
    },
    TIME("Time") {
        override fun fromJson(json: Json.Object): Generator {
            return TimeGenerator(
                json[SerializationConstants.FORMAT_KEY].getValueOrNull()
            )
        }
    },
    DATE_TIME("DateTime") {
        override fun fromJson(json: Json.Object): Generator {
            return DateTimeGenerator(
                json[SerializationConstants.FORMAT_KEY].getValueOrNull()
            )
        }
    },
    RANDOM_BOOLEAN("RandomBoolean") {
        override fun fromJson(json: Json.Object): Generator {
            return RandomBooleanGenerator
        }
    };

    abstract fun fromJson(json: Json.Object): Generator

    companion object {
        fun fromJson(json: Json): Generator {
            json as Json.Object
            val typeString = json[SerializationConstants.TYPE_KEY].getValue<String>()
            val type = values().firstOrNull { it.type.equals(typeString, ignoreCase = true) } ?: throw IllegalArgumentException("Unknown generator type $typeString")
            return type.fromJson(json)
        }
    }
}