package retrofit2

import au.com.dius.pact.external.PactBuildException
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.RetrofitPactRequestWithParams
import org.apache.http.entity.ContentType
import java.lang.reflect.Method

class RetrofitPactRequest(
    private val retrofit: Retrofit,
    method: Method
) {

    private val httpMethod: String
    private val hasBody: Boolean
    private val isFormEncoded: Boolean
    private val isMultipart: Boolean
    private val relativeUrl: String?
    private val headers: Headers
    private val contentType: MediaType?
    private val parameterHandlers: Array<ParameterHandler<Any?>>
    private val isKotlinSuspendFunction: Boolean

    init {
        val requestFactoryBuilder = try {
            RequestFactory.Builder(retrofit, method).apply { build() }
        } catch (e: Exception) {
            throw PactBuildException("Failed to build request from $method with retrofit", e)
        }
        httpMethod = requestFactoryBuilder.httpMethod!!
        hasBody = requestFactoryBuilder.hasBody
        isFormEncoded = requestFactoryBuilder.isFormEncoded
        isMultipart = requestFactoryBuilder.isMultipart
        relativeUrl = requestFactoryBuilder.relativeUrl
        headers = requestFactoryBuilder.headers ?: Headers.headersOf()
        contentType = requestFactoryBuilder.contentType
        parameterHandlers = requestFactoryBuilder.parameterHandlers as Array<ParameterHandler<Any?>>
        isKotlinSuspendFunction = requestFactoryBuilder.isKotlinSuspendFunction
    }

    fun applyParameterValues(vararg args: Any?): RetrofitPactRequestWithParams {
        var argumentCount = args.size
        require(argumentCount == parameterHandlers.size) {
            "Argument count $argumentCount doesn't match expected count ${parameterHandlers.size}"
        }
        val requestBuilder = RequestBuilder(
            httpMethod,
            retrofit.baseUrl,
            relativeUrl,
            headers,
            contentType,
            hasBody,
            isFormEncoded,
            isMultipart
        )
        if (isKotlinSuspendFunction) {
            // The Continuation is the last parameter and the handlers array contains null at that index.
            argumentCount--
        }

        for (argumentIndex in 0 until argumentCount) {
            parameterHandlers[argumentIndex].apply(requestBuilder, args[argumentIndex])
        }

        val request = requestBuilder.get()
        when {
            isFormEncoded -> request.addHeader(
                ContentType.CONTENT_TYPE,
                ContentType.APPLICATION_FORM_URLENCODED.mimeType.toString()
            )
            isMultipart -> request.addHeader(
                ContentType.CONTENT_TYPE,
                ContentType.MULTIPART_FORM_DATA.mimeType.toString()
            )
        }
        return RetrofitPactRequestWithParams(request.build())
    }
}
