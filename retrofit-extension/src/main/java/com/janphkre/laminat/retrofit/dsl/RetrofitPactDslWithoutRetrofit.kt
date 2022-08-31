package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import java.lang.reflect.Method
import retrofit2.Retrofit

class RetrofitPactDslWithoutRetrofit(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofitMethod: Method
) {

    @Suppress("unused")
    fun on(retrofit: Retrofit): RetrofitPactDsl {
        return RetrofitPactDsl(pactDslRequestWithoutPath, retrofitMethod, retrofit)
    }
}
