package au.com.dius.pact.external

import au.com.dius.pact.model.PactMergeException
import au.com.dius.pact.model.Response
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.CoroutineDispatcher
import org.apache.http.entity.ContentType
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.coroutines.CoroutineContext

class PactEngine(
    override val coroutineContext: CoroutineContext,
    override val dispatcher: CoroutineDispatcher,
    private val behaviour: PactEngineBehaviour,
    private val pactErrorCode: Int
): HttpClientEngine {

    override val config: HttpClientEngineConfig = HttpClientEngineConfig()

    override fun close() {
        behaviour.teardown()
    }

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        if (!behaviour.isActive) {
            throw IllegalStateException("Engine has been closed! Not accepting new requests.")
        }
        val requestTime = GMTDate()
        try {
            val incomingRequest = IncomingRequestImpl(data)
            val requestMatch = behaviour.createMatch(incomingRequest)
            return when (requestMatch) {
                is RequestMatch.FullRequestMatch -> {
                    behaviour.countMatch()
                    requestMatch.interaction.response.generateResponse().mapToResponseData(requestTime)
                }
                is RequestMatch.PartialRequestMatch -> {
                    notFoundResponse(requestTime, requestMatch.toErrorMessage())
                }
                is RequestMatch.RequestMismatch -> {
                    notFoundResponse(requestTime, requestMatch.toErrorMessage())
                }
            }
        } catch (e: PactMergeException) {
            return notFoundResponse(requestTime, e.message ?: "Unknown error while merging pact")
        } catch (e: Exception) {
            StringWriter().use { stringWriter ->
                e.printStackTrace(PrintWriter(stringWriter, true))
                return notFoundResponse(requestTime, stringWriter.toString())
            }
        }
    }

    private fun Response.mapToResponseData(requestTime: GMTDate): HttpResponseData {
        val charset = this.charset()
        return HttpResponseData(
            HttpStatusCode.fromValue(this.status),
            requestTime,
            this.headers.mapToHeaders(),
            HttpProtocolVersion.HTTP_1_1,
            this.body.asBinary(charset),
            coroutineContext
        )
    }

    private fun Map<String, String>?.mapToHeaders(): Headers {
        if (this == null) {
            return Headers.Empty
        }
        return Headers.build {
            this@mapToHeaders.forEach { (key, value) ->
                append(key, value)
            }
        }
    }

    private fun notFoundResponse(requestTime: GMTDate, body: Any?): HttpResponseData {
        behaviour.countError()
        return HttpResponseData(
            HttpStatusCode(pactErrorCode, "PactError"),
            requestTime,
            Headers.build { append(ContentType.CONTENT_TYPE, "${ContentType.DEFAULT_TEXT.mimeType}; charset=${Charsets.UTF_8.name()}") },
            HttpProtocolVersion.HTTP_1_1,
            body ?: "",
            coroutineContext
        )
    }
}