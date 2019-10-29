package au.com.dius.pact.external

import au.com.dius.pact.model.BasePact
import com.google.gson.JsonElement
import okhttp3.mockwebserver.RecordedRequest
import org.apache.http.entity.ContentType

/**
 * An incoming request is created by the server and passed into the PactDispatcher and RequestMatcher.
 * It provides methods to access all data of the (HTTP) request.
 * Internally it is represented by a RecordedRequest (okhttp3 Mockserver).
 *
 * @see PactDispatcher
 * @see RequestMatcher
 * @see RecordedRequest
 * @author Jan Phillip Kretzschmar
 */
class IncomingRequest(
    private val internalRequest: RecordedRequest
) {

    private val lazyBody: String? by lazy {
        internalRequest.body?.readUtf8()
    }

    private val lazyJson: JsonElement by lazy {
        BasePact.jsonParser.parse(lazyBody)
    }

    fun getMethod(): String {
        return internalRequest.method
    }

    fun getEncodedPath(): String {
        return internalRequest.requestUrl.encodedPath()
    }

    fun queryParameterValues(key: String): List<String> {
        return internalRequest.requestUrl.queryParameterValues(key)
    }

    fun queryParameterNames(): Set<String> {
        return internalRequest.requestUrl.queryParameterNames()
    }

    fun getHeaders(): Map<String, List<String>> {
        return internalRequest.headers.toMultimap()
    }

    fun getCookie(): List<String> {
        return internalRequest.headers.values("cookie")
    }

    fun getContentType(): String {
        return internalRequest.getHeader(ContentType.CONTENT_TYPE) ?: ""
    }

    fun getBodySize(): Long {
        return internalRequest.bodySize
    }

    fun getBody(): String? {
        return lazyBody
    }

    fun getBodyAsJson(): JsonElement {
        return lazyJson
    }
}