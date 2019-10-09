package com.janphkre.laminat.retrofit

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.external.PactJsonifier
import com.janphkre.laminat.retrofit.annotations.MatchBodyMinArrays
import com.janphkre.laminat.retrofit.annotations.MatchBodyRegexes
import com.janphkre.laminat.retrofit.annotations.data.MatchRegex
import com.janphkre.laminat.retrofit.annotations.data.MinArray
import org.junit.Assert
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.File

class RetrofitDslTest {

    data class Something(val abc: String)

    data class SomeOtherThing(
        val def: String,
        val ghi: Array<SomeThirdThing>,
        val jkl: Array<String>
    )

    data class SomeThirdThing(val thirdString: String)

    interface TestApi {
        @POST("api/v1/example")
        @MatchBodyRegexes([
            MatchRegex("$.def", "[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}"),
            MatchRegex("$.jkl[*]", "Hello.*")
        ])
        @MatchBodyMinArrays([MinArray("$.ghi", 1)])
        fun getExample(@Body body: SomeOtherThing): Something
    }

    private val retrofitInstance = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://localhost")
        .build()
    private val expectedPact = "testretrofitconsumer:testretrofitprovider.json"

    @Test
    fun retrofit_InstanciatePact_MatchesExampleRequest() {
        val pactInteraction = ConsumerPactBuilder("testretrofitconsumer")
            .hasPactWith("testretrofitprovider")
            .uponReceiving("POST example")
            .on(retrofitInstance)
            .match(TestApi::getExample)
            .withParameters(SomeOtherThing("01.01.2000", arrayOf(SomeThirdThing("String1")), arrayOf("Hello1", "Hello2")))
            .willRespondWith()
            .status(200)
            .body("{}")
            .toPact()

        Assert.assertNotNull(pactInteraction)

        PactJsonifier.generateJson(listOf(pactInteraction), File("pacts"))
        val outputPactFile = File("pacts/$expectedPact")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = readFile(outputPactFile)
        val expectedPact = readFile(File("src/test/assets/$expectedPact"))
        Assert.assertEquals("Generated pact does not match expectations!", expectedPact, outputPact)
    }
}