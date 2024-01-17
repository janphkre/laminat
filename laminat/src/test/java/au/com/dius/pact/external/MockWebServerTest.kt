package au.com.dius.pact.external

import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MockWebServerTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun mockWebServer_requestsPerformedAndShutdown_ServerShutsDown() {
        mockWebServer.start()
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .get()
            .url("http://${mockWebServer.hostName}:${mockWebServer.port}/test/path")
            .build()

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
        Assert.assertEquals(true, httpClient.newCall(request).execute().isSuccessful)

        val port = mockWebServer.port
        mockWebServer.shutdown()
        ServerSocket(port) // checks that the port has been freed
    }

    companion object {
        private const val TIMEOUT = 3L
    }
}