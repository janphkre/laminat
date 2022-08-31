package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.external.PactBuildException
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import retrofit2.Retrofit

@Suppress("unused")
fun <T> PactDslWithProvider.uponReceiving(
    retrofitMethod: KFunction<T>,
    retrofit: Retrofit
): RetrofitPactDsl {
    val javaRetrofitMethod = retrofitMethod.javaMethod
        ?: throw PactBuildException(
            "The given method $retrofitMethod can not be represented by a java method!"
        )
    return uponReceiving(javaRetrofitMethod, retrofit)
}

@Suppress("unused")
fun PactDslWithProvider.uponReceiving(
    retrofitMethod: Method,
    retrofit: Retrofit
): RetrofitPactDsl {
    return RetrofitPactDsl(
        uponReceiving(retrofitMethod.name),
        retrofitMethod,
        retrofit
    )
}

@Suppress("unused")
fun <T> PactDslRequestWithoutPath.uponReceiving(
    retrofitMethod: KFunction<T>
): RetrofitPactDslWithoutRetrofit {
    val javaRetrofitMethod = retrofitMethod.javaMethod
        ?: throw PactBuildException(
            "The given method $retrofitMethod can not be represented by a java method!"
        )
    return uponReceiving(javaRetrofitMethod)
}

@Suppress("unused")
fun PactDslRequestWithoutPath.uponReceiving(
    retrofitMethod: Method
): RetrofitPactDslWithoutRetrofit {
    return RetrofitPactDslWithoutRetrofit(this, retrofitMethod)
}

@Suppress("unused")
fun PactDslRequestWithoutPath.on(
    retrofit: Retrofit
): RetrofitPactDslWithoutMethod {
    return RetrofitPactDslWithoutMethod(this, retrofit)
}
