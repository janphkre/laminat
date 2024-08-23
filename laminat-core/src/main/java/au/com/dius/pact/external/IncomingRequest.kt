package au.com.dius.pact.external

import au.com.dius.pact.external.json.Json
import okhttp3.mockwebserver.RecordedRequest
import java.nio.charset.Charset

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
interface IncomingRequest {

    fun getMethod(): String?

    fun getEncodedPath(): String?

    fun queryParameterValues(key: String): List<String?>

    fun queryParameterNames(): Set<String>

    fun getCompleteHeaders(): Map<String, List<String>>

    fun getCookie(): List<String>

    fun getContentType(): String

    fun getBodySize(): Long

    fun getBody(): ByteArray?

    fun getBodyCharset(): Charset

    fun getBodyAsString(): String?
}