package au.com.dius.pact.external

import au.com.dius.pact.external.util.toReader
import au.com.dius.pact.model.HttpPart
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.matchingrules.MatchingRules
import io.ktor.client.request.HttpRequestData
import io.ktor.http.content.OutgoingContent
import io.ktor.util.toMap
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.readFully
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset

class IncomingRequestImpl(
    private val internalRequest: HttpRequestData
) : IncomingRequest, HttpPart() {

    private val charset: Charset by lazy {
        charset()
    }

    private val lazyHeaders: Map<String, List<String>> by lazy {
        val headers  = mutableMapOf<String, List<String>>()
        internalRequest.body.contentLength?.let { contentLength ->
            headers.put("Content-Length", listOf(contentLength.toString()))
        }
        internalRequest.body.contentType?.let { contentType ->
            headers.put("Content-Type", listOf(contentType.toString()))
        }
        internalRequest.headers.entries().forEach {
            headers.put(it.key, it.value)
        }
        headers
    }

    private val lazyBody: ByteArray? by lazy {
        val body = internalRequest.body
        when(body) {
            is OutgoingContent.ByteArrayContent -> body.bytes()
            is OutgoingContent.NoContent -> null
            is OutgoingContent.ProtocolUpgrade -> null
            is OutgoingContent.ReadChannelContent -> {
                runBlocking {
                    val channel = body.readFrom()
                    while (!channel.isClosedForWrite) {
                        channel.awaitContent()
                    }
                    val byteArray = ByteArray(channel.availableForRead)
                    channel.readFully(byteArray)
                    byteArray
                }
            }
            is OutgoingContent.WriteChannelContent -> {
                runBlocking {
                    val channel = ByteChannel()
                    body.writeTo(channel)
                    channel.flush()
                    channel.close()
                    val byteArray = ByteArray(channel.availableForRead)
                    channel.readFully(byteArray)
                    byteArray
                }
            }
        }
    }

    private val lazyBodyString: String? by lazy {
        getBody()?.toReader(charset)?.readText()
    }

    override fun getMethod(): String {
        return internalRequest.method.value
    }

    override fun getEncodedPath(): String {
        return internalRequest.url.encodedPath
    }

    override fun queryParameterValues(key: String): List<String?> {
        return internalRequest.url.parameters.getAll(key) ?: emptyList()
    }

    override fun queryParameterNames(): Set<String> {
        return internalRequest.url.parameters.names()
    }

    override fun getCompleteHeaders(): Map<String, List<String>> {
        return lazyHeaders
    }

    override fun getCookie(): List<String> {
        return internalRequest.headers.getAll("cookie") ?: emptyList()
    }

    override fun getContentType(): String {
        return internalRequest.body.contentType?.toString() ?: ""
    }

    override fun getBodySize(): Long {
        return internalRequest.body.contentLength ?: 0L
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