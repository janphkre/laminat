package au.com.dius.pact.model

import kotlin.jvm.JvmOverloads

/**
 * Pact Provider
 */
data class Provider @JvmOverloads constructor(val name: String = "provider") {

    fun toMap(): Map<String, Any?> {
        return mapOf(Pair("name", name))
    }
}