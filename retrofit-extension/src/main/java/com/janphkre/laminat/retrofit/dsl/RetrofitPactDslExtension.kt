package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.external.PactBuildException
import retrofit2.Retrofit
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun <T> PactDslWithProvider.uponReceiving(retrofitMethod: KFunction<T>, retrofit: Retrofit): RetrofitPactDsl {
    val javaRetrofitMethod = retrofitMethod.javaMethod ?: throw PactBuildException("The given method $retrofitMethod can not be represented by a java method!")
    return RetrofitPactDsl(
        uponReceiving(retrofitMethod.name),
        javaRetrofitMethod,
        retrofit
    )
}

fun PactDslRequestWithoutPath.on(retrofit: Retrofit): RetrofitPactDslWithoutMethod {
    return RetrofitPactDslWithoutMethod(this, retrofit)
}