package retrofit2

import au.com.dius.pact.external.PactBuildException
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.RetrofitPactRequestWithParams
import java.lang.reflect.Method

class RetrofitPactRequest(
    private val retrofit: Retrofit,
    method: Method
) {

//    private val gotField: Boolean
//    private val gotPart: Boolean
//    private val gotBody: Boolean
//    private val gotPath: Boolean
//    private val gotQuery: Boolean
//    private val gotQueryName: Boolean
//    private val gotQueryMap: Boolean
//    private val gotUrl: Boolean
    private val httpMethod: String
    private val hasBody: Boolean
    private val isFormEncoded: Boolean
    private val isMultipart: Boolean
    private val relativeUrl: String
    private val headers: Headers
    private val contentType: MediaType?
//    private val relativeUrlParamNames: Set<String>
    private val parameterHandlers: Array<ParameterHandler<Any?>>
    private val isKotlinSuspendFunction: Boolean

    init {
        val requestFactoryBuilder = try {
            RequestFactory.Builder(retrofit, method).apply { build() }
        } catch (e: Exception) {
            throw PactBuildException("Failed to build request from $method with retrofit", e)
        }
//        gotField = requestFactoryBuilder.gotField
//        gotPart = requestFactoryBuilder.gotPart
//        gotBody = requestFactoryBuilder.gotBody
//        gotPath = requestFactoryBuilder.gotPath
//        gotQuery = requestFactoryBuilder.gotQuery
//        gotQueryName = requestFactoryBuilder.gotQueryName
//        gotQueryMap = requestFactoryBuilder.gotQueryMap
//        gotUrl = requestFactoryBuilder.gotUrl
        httpMethod = requestFactoryBuilder.httpMethod!!
        hasBody = requestFactoryBuilder.hasBody
        isFormEncoded = requestFactoryBuilder.isFormEncoded
        isMultipart = requestFactoryBuilder.isMultipart
        relativeUrl = requestFactoryBuilder.relativeUrl!!
        headers = requestFactoryBuilder.headers!!
        contentType = requestFactoryBuilder.contentType
//        relativeUrlParamNames = requestFactoryBuilder.relativeUrlParamNames ?: emptySet()
        parameterHandlers = requestFactoryBuilder.parameterHandlers as Array<ParameterHandler<Any?>>
        isKotlinSuspendFunction = requestFactoryBuilder.isKotlinSuspendFunction
    }

    fun applyParameterValues(vararg args: Any?): RetrofitPactRequestWithParams {
        var argumentCount = args.size
        if (argumentCount != parameterHandlers.size) {
            throw IllegalArgumentException("Argument count ($argumentCount) doesn't match expected count (${parameterHandlers.size}")
        }
        val requestBuilder = RequestBuilder(httpMethod, retrofit.baseUrl, relativeUrl,
            headers, contentType, hasBody, isFormEncoded, isMultipart)
        if (isKotlinSuspendFunction) {
            // The Continuation is the last parameter and the handlers array contains null at that index.
            argumentCount--
        }

        for (argumentIndex in 0 until argumentCount) {
            parameterHandlers[argumentIndex].apply(requestBuilder, args[argumentIndex])
        }

        return RetrofitPactRequestWithParams(requestBuilder.get())
    }
}