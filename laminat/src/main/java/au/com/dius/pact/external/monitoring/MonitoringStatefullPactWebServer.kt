package au.com.dius.pact.external.monitoring

import android.util.Log
import au.com.dius.pact.external.StatefullPactWebServer
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

/**
 * A monitoring server provides additional functionality over a statefull pact web server
 * in such a way that incoming request and their response are measured in milliseconds.
 * You should keep in mind that a very complex regular expression in your pacts may be
 * one of the main reasons for slow matching.
 *
 * @author Jan Phillip Kretzschmar
 */
class MonitoringStatefullPactWebServer(allowUnexpectedKeys: Boolean, pactErrorCode: Int)
    : StatefullPactWebServer(allowUnexpectedKeys, pactErrorCode) {

    init {
        mockWebServer.setDispatcher(MonitoringDispatcher(dispatcher))
    }

    private class MonitoringDispatcher(
        private val wrapped: Dispatcher
    ) : Dispatcher() {

        private val tag = "PactWebServer"

        override fun dispatch(request: RecordedRequest?): MockResponse {
            Log.d(tag, "Received request for ${request?.method} ${request?.path}")
            val startMs = System.currentTimeMillis()
            val result = wrapped.dispatch(request)
            Log.d(tag, "Generated result in ${System.currentTimeMillis() - startMs} ms.")
            return result
        }
    }
}