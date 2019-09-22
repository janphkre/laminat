package com.janphkre.laminat.retrofit

import org.junit.Test
import retrofit2.http.GET

class RetrofitDslTest {

    data class Something(val abc: String)

    interface TestApi {
        @GET("api/v1/example")
        fun getExample(): Something
    }

    @Test
    fun addition_isCorrect() {
        val pactInteraction = TestApi::getExample.toPact()
    }
}
