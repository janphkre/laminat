package au.com.dius.pact.external

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.util.toReader
import au.com.dius.pact.model.HttpPart
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import java.nio.charset.Charset
import okhttp3.mockwebserver.RecordedRequest
import org.apache.http.entity.ContentType

class IncomingRequestImpl(
    private val internalRequest: RecordedRequest
) : IncomingRequest, HttpPart() {

    private val charset: Charset by lazy {
        charset()
    }

    private val lazyBody: ByteArray? by lazy {
        internalRequest.body.readByteArray()
    }

    private val lazyBodyString: String? by lazy {
        getBody()?.toReader(charset)?.readText()
    }

    override fun getMethod(): String? {
        return internalRequest.method
    }

    override fun getEncodedPath(): String? {
        return internalRequest.requestUrl?.encodedPath
    }

    override fun queryParameterValues(key: String): List<String?> {
        return internalRequest.requestUrl?.queryParameterValues(key) ?: emptyList()
    }

    override fun queryParameterNames(): Set<String> {
        return internalRequest.requestUrl?.queryParameterNames ?: emptySet()
    }

    override fun getCompleteHeaders(): Map<String, List<String>> {
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

    override fun getBody(): ByteArray? {
        return lazyBody
    }

    override fun getBodyCharset(): Charset {
        return charset
    }

    override fun getBodyAsString(): String? {
        return lazyBodyString
    }

    override val body: OptionalBody
        get() = OptionalBody.body(getBody())
    override val headers: Map<String, String> = getCompleteHeaders().mapValues { it.value.last() }
    override val matchingRules: MatchingRules
        get() { throw NotImplementedError() }
}