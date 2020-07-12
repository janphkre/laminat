package au.com.dius.pact.external

import au.com.dius.pact.util.json.JsonElement

interface IncomingRequest {
    fun getMethod(): String
    fun getEncodedPath(): String
    fun queryParameterValues(key: String): List<String>
    fun queryParameterNames(): Set<String>
    fun getHeaders(): Map<String, List<String>>
    fun getCookie(): List<String>
    fun getContentType(): String
    fun getBodySize(): Long
    fun getBody(): String?
    fun getBodyAsJson(): JsonElement
}