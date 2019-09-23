package com.janphkre.laminat.retrofit

import au.com.dius.pact.consumer.ConsumerPactBuilder
import com.janphkre.laminat.retrofit.dsl.on
import org.junit.Assert
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET

class RetrofitDslTest {

    data class Something(val abc: String)

    interface TestApi {
        @GET("api/v1/example")
        fun getExample(): Something
    }

    private val retrofitInstance = Retrofit.Builder()
        .build()

    @Test
    fun addition_isCorrect() {
        val pactInteraction = ConsumerPactBuilder("testconsumer")
            .hasPactWith("testprovider")
            .uponReceiving("GET example")
            .on(retrofitInstance)
            .match(TestApi::getExample)

        Assert.assertNotNull(pactInteraction)
        TODO()
    }
}