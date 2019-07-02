package au.com.dius.pact.model

/**
 * Class that encapsulates all the info about a provider state
 *
 * name - The provider state description
 * params - Provider state parameters as key value pairs
 */
data class ProviderState(val name: String, val params: Map<String, Any> = mapOf()) {

    constructor(name: String?) : this(name ?: "None")

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>("name" to name)
        if (params.isNotEmpty()) {
            map["params"] = params
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
}