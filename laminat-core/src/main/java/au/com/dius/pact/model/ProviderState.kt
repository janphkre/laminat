package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValue
import au.com.dius.pact.model.serialization.SerializationConstants

/**
 * Class that encapsulates all the info about a provider state
 *
 * name - The provider state description
 * params - Provider state parameters as key value pairs
 */
data class ProviderState(
    val name: String,
    val params: Map<String, Any>
) {

    constructor(name: String?) : this(nonNullName(name), mapOf())

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(SerializationConstants.NAME_KEY to name)
        if (params.isNotEmpty()) {
            map[SerializationConstants.PARAMS_KEY] = params
        }
        return map
    }

    fun matches(state: String) = name.matches(Regex(state))

    override fun equals(other: Any?): Boolean {
        if (other !is ProviderState) {
            return false
        }
        return other.name == this.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object {

        private const val NONE = "None"

        fun nonNullName(name: String?): String {
            if (name.isNullOrEmpty()) {
                return NONE
            }
            return name
        }

        fun fromJson(json: Json): ProviderState {
            return ProviderState(
                json[SerializationConstants.NAME_KEY].getValue<String>()
            )
        }
    }
}