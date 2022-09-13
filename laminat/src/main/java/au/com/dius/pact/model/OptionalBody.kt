package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import java.nio.charset.Charset

/**
 * Class to represent missing, empty, null and present bodies
 */
sealed interface OptionalBody {

    object MissingBody : OptionalBody {
        override fun asBinary(charset: Charset): ByteArray {
            return ByteArray(0)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is OptionalBody) {
                return false
            }

            return true
        }
    }

    object NullBody : OptionalBody {
        override fun asBinary(charset: Charset): ByteArray {
            return ByteArray(0)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is OptionalBody) {
                return false
            }
            if (!asBinary(Charsets.UTF_8).contentEquals(other.asBinary(Charsets.UTF_8))) {
                return false
            }

            return true
        }
    }

    object EmptyBody : OptionalBody {

        override fun asBinary(charset: Charset): ByteArray {
            return ByteArray(0)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is OptionalBody) {
                return false
            }
            if (!asBinary(Charsets.UTF_8).contentEquals(other.asBinary(Charsets.UTF_8))) {
                return false
            }

            return true
        }
    }

    data class StringBody(
        private val value: String
    ) : OptionalBody {

        private val parsedBodyAsJson: Json by lazy {
            Json.parse(value)
        }

        override fun asBinary(charset: Charset): ByteArray {
            return unwrap().toByteArray(charset)
        }

        fun unwrap(): String {
            return value
        }

        fun unwrapJson(): Json {
            return parsedBodyAsJson
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is OptionalBody) {
                return false
            }
            if (!asBinary(Charsets.UTF_8).contentEquals(other.asBinary(Charsets.UTF_8))) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            return asBinary(Charsets.UTF_8).contentHashCode()
        }
    }

    data class BinaryBody(
        private val value: ByteArray
    ) : OptionalBody {

        override fun asBinary(charset: Charset): ByteArray {
            return unwrap()
        }

        fun unwrap(): ByteArray {
            return value
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is OptionalBody) {
                return false
            }
            if (!asBinary(Charsets.UTF_8).contentEquals(other.asBinary(Charsets.UTF_8))) {
                return false
            }

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

    fun asBinary(charset: Charset): ByteArray
}