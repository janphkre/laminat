package au.com.dius.pact.external

import io.ktor.client.request.HttpRequestData
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.content.ByteArrayContent
import io.ktor.util.Attributes
import io.ktor.util.InternalAPI
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import java.nio.charset.Charset

@OptIn(InternalAPI::class)
abstract class AbstractRequestTest {

    protected fun createIncomingRequest(
        requestBody: ByteArray? = null,
        method: String = "POST",
        authorization: String = "",
        contentType: String = "application/json",
        path: String = "/test/path"
    ): IncomingRequest {
        return mockk {
            every { getMethod() } returns method
            every { getEncodedPath() } returns path
            every { getBody() } returns requestBody
            every { getBodySize() } returns (requestBody?.size?.toLong() ?: 0L)
            every { getCompleteHeaders() } returns mapOf(
                "Authorization" to listOf(authorization),
                "Content-Type" to listOf(contentType),
                "Content-Length" to listOf((requestBody?.size ?: 0).toString()),
                "Host" to listOf("localhost:41163"),
                "Connection" to listOf("Keep-Alive"),
                "Accept-Encoding" to listOf("gzip"),
                "User-Agent" to listOf("test/request/matcher"),
                "Accept-Language" to listOf("de"),
            )
            every { queryParameterNames() } returns emptySet()
            every { queryParameterValues(any()) } returns emptyList()
            every { getCookie() } returns emptyList()
            every { getBodyAsString() } answers { requestBody?.let { String(requestBody) } }
            every { getContentType() } returns contentType
            every { getBodyCharset() } returns Charset.forName("UTF-8")
        }
    }

    protected fun createGetKtorRequest(): HttpRequestData {
        return HttpRequestData(
            url = Url("http://exampleHostname:12345/test/path"),
            method = HttpMethod.Get,
            headers = Headers.Empty,
            body = EmptyContent,
            executionContext = Job(),
            attributes = Attributes()
        )
    }

    protected fun createPostKtorRequest(body: ByteArray): HttpRequestData {
        return HttpRequestData(
            url = Url("http://exampleHostname:12345/test/path"),
            method = HttpMethod.Post,
            headers = Headers.Empty,
            body = ByteArrayContent(body),
            executionContext = Job(),
            attributes = Attributes()
        )
    }

    protected fun createPostKtorRequestWithFullData(body: ByteArray): HttpRequestData {
        return HttpRequestData(
            url = Url("http://exampleHostname:12345/test/path"),
            method = HttpMethod.Post,
            headers = Headers.Empty,
            body = ByteArrayContent(body, ContentType.parse("application/json")),
            executionContext = Job(),
            attributes = Attributes()
        )
    }
}