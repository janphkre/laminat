package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.consumer.dsl.PactDslResponse
import au.com.dius.pact.external.PactBuildException
import retrofit2.Retrofit
import retrofit2.RetrofitPactRequest
import java.lang.reflect.Method

class RetrofitPactDsl(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofitMethod: Method,
    retrofit: Retrofit
) {

    private val retrofitRequest: RetrofitPactRequest

    init {
        try {
            retrofitRequest = RetrofitPactRequest(retrofit, retrofitMethod)
        } catch (e: Exception) {
            raiseException("The method could not be processed by retrofit", e)
        }
    }

    fun withParameters(vararg parameterValues: Any?): RetrofitPactDslWithParams {
        val paramsRetrofitRequest = try {
            retrofitRequest.applyParameterValues(*parameterValues)
        } catch (e: Exception) {
            raiseException("The parameters could not be processed by retrofit", e)
        }

        return RetrofitPactDslWithParams(
            pactDslRequestWithoutPath,
            retrofitMethod,
            paramsRetrofitRequest
        )
    }

    fun willRespondWith(): PactDslResponse {
        return withParameters().willRespondWith()
    }

    private fun raiseException(message: String, cause: Exception? = null): Nothing {
        throw PactBuildException("$message in $retrofitMethod", cause)
    }
}