package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.external.PactBuildException
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import retrofit2.Retrofit

class RetrofitPactDslWithoutMethod(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofit: Retrofit
) {

    @Suppress("unused")
    fun <T> match(retrofitMethod: KFunction<T>): RetrofitPactDsl {
        val javaRetrofitMethod = retrofitMethod.javaMethod
            ?: throw PactBuildException(
                "The given method $retrofitMethod can not be represented by a java method!"
            )
        return match(javaRetrofitMethod)
    }

    @Suppress("unused")
    fun match(retrofitMethod: Method): RetrofitPactDsl {
        return RetrofitPactDsl(
            pactDslRequestWithoutPath,
            retrofitMethod,
            retrofit
        )
    }
}
