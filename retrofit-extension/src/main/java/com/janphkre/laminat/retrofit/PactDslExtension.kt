package com.janphkre.laminat.retrofit

import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import retrofit2.Retrofit
import kotlin.reflect.KFunction

fun <T> PactDslRequestWithoutPath.uponReceiving(retrofitMethod: KFunction<T>, retrofit: Retrofit): RetrofitPactDsl<T> {
    return RetrofitPactDsl(this, retrofitMethod, retrofit)
}

//RESPONSE:
//retrofitMethod.returnType ;TODO("Convert return type to body of response")