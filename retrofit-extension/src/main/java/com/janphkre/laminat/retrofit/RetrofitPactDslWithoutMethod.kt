package com.janphkre.laminat.retrofit

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.external.PactBuildException
import retrofit2.Retrofit
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class RetrofitPactDslWithoutMethod(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofit: Retrofit
) {

    fun <T> match(retrofitMethod: KFunction<T>): RetrofitPactDsl {
        val javaRetrofitMethod = retrofitMethod.javaMethod ?: throw PactBuildException("The given method $retrofitMethod can not be represented by a java method!")
        return RetrofitPactDsl(pactDslRequestWithoutPath, javaRetrofitMethod, retrofit)
    }

    fun match(retrofitMethod: Method): RetrofitPactDsl {
        return RetrofitPactDsl(pactDslRequestWithoutPath, retrofitMethod, retrofit)
    }
}