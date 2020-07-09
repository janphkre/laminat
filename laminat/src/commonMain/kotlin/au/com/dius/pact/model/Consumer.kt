package au.com.dius.pact.model

import kotlin.jvm.JvmOverloads

/**
 * Pact Consumer
 */
data class Consumer @JvmOverloads constructor(val name: String = "consumer") {

    fun toMap(): Map<String, Any?> {
        return mapOf(Pair("name", name))
    }
}