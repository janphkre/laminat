package au.com.dius.pact.external

import java.net.InetAddress
import java.net.Socket
import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.mockito.Mockito

abstract class AbstractRequestTest {

    protected fun getMockSocket(): Socket {
        val mockInetAddress = Mockito.mock(InetAddress::class.java)
        Mockito.doReturn("mockhost").`when`(mockInetAddress).hostName

        val mockSocket = Mockito.mock(Socket::class.java)
        Mockito.doReturn(mockInetAddress).`when`(mockSocket).inetAddress
        Mockito.doReturn(mockInetAddress).`when`(mockSocket).localAddress
        Mockito.doReturn(1234).`when`(mockSocket).localPort

        return mockSocket
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

    protected fun getIncomingRequest(
        requestBody: ByteArray,
        method: String = "POST",
        authorization: String = "",
        contentType: String = "application/json"
    ): IncomingRequest {
        val recordedRequest = getRecordedRequest(requestBody, method, authorization, contentType)
        return IncomingRequestImpl(recordedRequest)
    }
}