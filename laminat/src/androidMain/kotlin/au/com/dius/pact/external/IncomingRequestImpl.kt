package au.com.dius.pact.external

import au.com.dius.pact.util.json.JsonElement
import au.com.dius.pact.util.json.JsonParser
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
class IncomingRequestImpl(
    private val internalRequest: RecordedRequest
) : IncomingRequest {

    private val lazyBody: String? by lazy {
        internalRequest.body?.readUtf8()
    }

    private val lazyJson: JsonElement by lazy {
        JsonParser.parse(lazyBody)
    }

    override fun getMethod(): String {
        return internalRequest.method
    }

    override fun getEncodedPath(): String {
        return internalRequest.requestUrl.encodedPath()
    }

    override fun queryParameterValues(key: String): List<String> {
        return internalRequest.requestUrl.queryParameterValues(key)
    }

    override fun queryParameterNames(): Set<String> {
        return internalRequest.requestUrl.queryParameterNames()
    }

    override fun getHeaders(): Map<String, List<String>> {
        return internalRequest.headers.toMultimap()
    }

    override fun getCookie(): List<String> {
        return internalRequest.headers.values("cookie")
    }

    override fun getContentType(): String {
        return internalRequest.getHeader(ContentType.CONTENT_TYPE) ?: ""
    }

    override fun getBodySize(): Long {
        return internalRequest.bodySize
    }

    override fun getBody(): String? {
        return lazyBody
    }

    override fun getBodyAsJson(): JsonElement {
        return lazyJson
    }
}