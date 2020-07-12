package au.com.dius.pact.model.requests

import au.com.dius.pact.model.exceptions.UnwrapMissingBodyException
import au.com.dius.pact.util.json.JsonElement
import au.com.dius.pact.util.json.JsonParser
import kotlin.jvm.JvmStatic

/**
 * Class to represent missing, empty, null and present bodies
 */
data class OptionalBody(val state: State, val value: String? = null) {

    val parsedBodyAsJson: JsonElement by lazy {
        JsonParser.parse(unwrap())
    }

    enum class State {
        MISSING, EMPTY, NULL, PRESENT
    }

    companion object {

        @JvmStatic
        fun missing(): OptionalBody {
            return OptionalBody(State.MISSING)
        }

        @JvmStatic
        fun empty(): OptionalBody {
            return OptionalBody(State.EMPTY, "")
        }

        @JvmStatic
        fun nullBody(): OptionalBody {
            return OptionalBody(State.NULL)
        }

        @JvmStatic
        fun body(body: String?): OptionalBody {
            return if (body == null) {
                nullBody()
            } else if (body.isEmpty()) {
                empty()
            } else {
                OptionalBody(State.PRESENT, body)
            }
        }
    }

    fun isMissing(): Boolean {
        return state == State.MISSING
    }

    fun isEmpty(): Boolean {
        return state == State.EMPTY
    }

    fun isNull(): Boolean {
        return state == State.NULL
    }

    fun isPresent(): Boolean {
        return state == State.PRESENT
    }

    fun orElse(defaultValue: String): String {
        return if (state == State.EMPTY || state == State.PRESENT) {
            value!!
        } else {
            defaultValue
        }
    }

    fun unwrap(): String {
        if (isPresent()) {
            return value!!
        } else {
            throw UnwrapMissingBodyException("Failed to unwrap value from a $state body")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is OptionalBody) {
            return false
        }
        return this.state == other.state &&
            this.value == other.value
    }
}