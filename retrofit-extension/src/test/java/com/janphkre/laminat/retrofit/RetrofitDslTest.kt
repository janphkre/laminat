package com.janphkre.laminat.retrofit

import au.com.dius.pact.BuildConfig
import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.external.PactJsonifier
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMinArray
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMinArrays
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyRegexes
import com.janphkre.laminat.retrofit.annotations.body.MatchRegex
import com.janphkre.laminat.retrofit.annotations.header.MatchHeader
import com.janphkre.laminat.retrofit.annotations.header.MatchHeaders
import com.janphkre.laminat.retrofit.dsl.on
import java.io.File
import java.nio.charset.Charset
import org.apache.http.Consts
import org.junit.After
import org.junit.Assert
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

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
        @MatchBodyRegexes(
            [
                MatchRegex("$.def", "[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}"),
                MatchRegex("$.jkl[*]", "Hello.*")
            ]
        )
        @MatchBodyMinArrays([MatchBodyMinArray("$.ghi", 1)])
        fun postExample(@Body body: SomeOtherThing): Something

        @POST("api/v1/formExample")
        @FormUrlEncoded
        @MatchBodyRegexes(
            [
                MatchRegex("$", "field1=.*&field2=.*")
            ]
        )
        fun postFormExample(
            @Field("field1") field1: String,
            @Field("field2") field2: String
        ): Something

        @POST("api/v1/emptyExample")
        @MatchHeaders(
            [
                MatchHeader("X-Foo", "Bar")
            ]
        )
        @Headers("X-Foo: Bar")
        fun postEmptyExample(): Something

        @GET
        fun getFullCustomUrl(@Url url: String): Something
    }

    private val retrofitInstance = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://localhost")
        .build()
    private val expectedPactName = "testretrofitconsumer___testretrofitprovider.json"

    @After
    fun teardown() {
        File("pacts").delete()
    }

    @Test
    fun retrofit_InstanciatePact_MatchesExampleRequest() {
        val pactInteraction = ConsumerPactBuilder("testretrofitconsumer")
            .hasPactWith("testretrofitprovider")
            .uponReceiving("POST example")
            .on(retrofitInstance)
            .match(TestApi::postExample)
            .withParameters(
                SomeOtherThing(
                    "01.01.2000",
                    arrayOf(SomeThirdThing("String1")),
                    arrayOf("Hello1", "Hello2")
                )
            )
            .willRespondWith()
            .status(200)
            .body("{}")
            .toPact()

        Assert.assertNotNull(pactInteraction)

        PactJsonifier.generateJson(listOf(pactInteraction), File("pacts"))
        val outputPactFile = File("pacts/$expectedPactName")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPactJson = File("src/test/assets/pact___jsonexample.json")
            .readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPact = updateVersion(expectedPactJson)
        Assert.assertEquals(
            "Generated pact does not match expectations!",
            expectedPact,
            outputPact
        )
    }

    @Test
    fun retrofit_InstanciatePact_MatchesFormExampleRequest() {
        val pactInteraction = ConsumerPactBuilder("testretrofitconsumer")
            .hasPactWith("testretrofitprovider")
            .uponReceiving("POST form example")
            .on(retrofitInstance)
            .match(TestApi::postFormExample)
            .withParameters("Hello1", "Hello2")
            .willRespondWith()
            .status(200)
            .body("{}")
            .toPact()

        Assert.assertNotNull(pactInteraction)

        PactJsonifier.generateJson(listOf(pactInteraction), File("pacts"))
        val outputPactFile = File("pacts/$expectedPactName")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPactJson = File("src/test/assets/pact___formexample.json")
            .readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPact = updateVersion(expectedPactJson)
        Assert.assertEquals(
            "Generated pact does not match expectations!",
            expectedPact,
            outputPact
        )
    }

    @Test
    fun retrofit_InstanciatePact_MatchesEmptyExampleRequest() {
        val pactInteraction = ConsumerPactBuilder("testretrofitconsumer")
            .hasPactWith("testretrofitprovider")
            .uponReceiving("POST empty example")
            .on(retrofitInstance)
            .match(TestApi::postEmptyExample)
            .willRespondWith()
            .status(200)
            .body("{}")
            .toPact()

        Assert.assertNotNull(pactInteraction)

        PactJsonifier.generateJson(listOf(pactInteraction), File("pacts"))
        val outputPactFile = File("pacts/$expectedPactName")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPactJson = File("src/test/assets/pact___emptyexample.json")
            .readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPact = updateVersion(expectedPactJson)
        Assert.assertEquals(
            "Generated pact does not match expectations!",
            expectedPact,
            outputPact
        )
    }

    @Test
    fun retrofit_InstanciatePact_MatchesUrlRequest() {
        val pactInteraction = ConsumerPactBuilder("testretrofitconsumer")
            .hasPactWith("testretrofitprovider")
            .uponReceiving("GET URL example")
            .on(retrofitInstance)
            .match(TestApi::getFullCustomUrl)
            .withParameters("http://example.org/example")
            .willRespondWith()
            .status(200)
            .body("{}")
            .toPact()

        Assert.assertNotNull(pactInteraction)

        PactJsonifier.generateJson(listOf(pactInteraction), File("pacts"))
        val outputPactFile = File("pacts/$expectedPactName")
        Assert.assertTrue("Pact was not generated!", outputPactFile.exists())

        val outputPact = outputPactFile.readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPactJson = File("src/test/assets/pact___urlexample.json")
            .readText(Charset.forName(Consts.UTF_8.name()))
        val expectedPact = updateVersion(expectedPactJson)
        Assert.assertEquals(
            "Generated pact does not match expectations!",
            expectedPact,
            outputPact
        )
    }

    private fun updateVersion(expectedPactJson: String): String {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        val expectedPactTree = gson.fromJson<JsonObject>(expectedPactJson, JsonObject::class.java)
        expectedPactTree.getAsJsonObject("metadata")
            .getAsJsonObject("pact-laminat-android")
            .addProperty("version", BuildConfig.VERSION_NAME)
        return gson.toJson(expectedPactTree)
    }
}
