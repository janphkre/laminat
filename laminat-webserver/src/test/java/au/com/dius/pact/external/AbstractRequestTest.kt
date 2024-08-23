package au.com.dius.pact.external

import io.mockk.every
import io.mockk.mockk
import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.net.InetAddress
import java.net.Socket

abstract class AbstractRequestTest {

    protected fun getMockSocket(): Socket {
        val mockInetAddress = mockk<InetAddress> {
            every { hostName } returns "mockhost"
        }
        return mockk {
            every { localPort } returns 1234
            every { inetAddress } returns mockInetAddress
            every { localAddress } returns mockInetAddress
        }
    }

    protected fun getRecordedRequest(
        requestBody: ByteArray,
        method: String = "POST",
        authorization: String = "",
        contentType: String = "application/json"
    ): RecordedRequest {
        val mockSocket = getMockSocket()
        val headers = Headers.Builder()
            .add("Authorization: $authorization")
            .add("Content-Type: $contentType")
            .add("Content-Length: ${requestBody.size}")
            .add("Host: localhost:41163")
            .add("Connection: Keep-Alive")
            .add("Accept-Encoding: gzip")
            .add("User-Agent: okhttp/3.9.0")
            .add("Accept-Language: de")
            .build()
        val body = Buffer()
        body.outputStream().use {
            it.write(requestBody)
        }
        return RecordedRequest("$method /test/path HTTP/1.1", headers, ArrayList(), body.size, body, 0, mockSocket)
    }
}