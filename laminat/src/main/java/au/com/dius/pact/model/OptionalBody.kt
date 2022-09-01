package au.com.dius.pact.model

import au.com.dius.pact.matchers.toUtf8ByteArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

/**
 * Class to represent missing, empty, null and present bodies
 */
sealed interface OptionalBody {

    object MissingBody : OptionalBody {
        override fun orEmptyBinary(): ByteArray {
            return ByteArray(0)
        }
    }

    object NullBody : OptionalBody {
        override fun orEmptyBinary(): ByteArray {
            return ByteArray(0)
        }
    }

    object EmptyBody : OptionalBody {

        override fun orEmptyBinary(): ByteArray {
            return ByteArray(0)
        }
    }

    data class StringBody(
        private val value: String
    ) : OptionalBody {

        private val parsedBodyAsJson: JsonElement by lazy {
            JsonParser.parseString(value)
        }

        override fun orEmptyBinary(): ByteArray {
            return unwrap().toUtf8ByteArray()
        }

        fun unwrap(): String {
            return value
        }

        fun unwrapJson(): JsonElement {
            return parsedBodyAsJson
        }
    }

    data class BinaryBody(
        private val value: ByteArray
    ) : OptionalBody {

        override fun orEmptyBinary(): ByteArray {
            return unwrap()
        }

        fun unwrap(): ByteArray {
            return value
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BinaryBody

            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    companion object {

        @JvmStatic
        fun missing(): OptionalBody {
            return MissingBody
        }

        @JvmStatic
        fun empty(): OptionalBody {
            return EmptyBody
        }

        @JvmStatic
        fun nullBody(): OptionalBody {
            return NullBody
        }

        @JvmStatic
        fun body(body: ByteArray?): OptionalBody {
            return if (body == null) {
                nullBody()
            } else if (body.isEmpty()) {
                empty()
            } else {
                BinaryBody(body)
            }
        }

        @JvmStatic
        fun body(body: String?): OptionalBody {
            return if (body == null) {
                nullBody()
            } else if (body.isEmpty()) {
                empty()
            } else {
                StringBody(body)
            }
        }
    }

    fun isPresent(): Boolean {
        return this is StringBody || this is BinaryBody
    }

    fun orEmptyBinary(): ByteArray
}