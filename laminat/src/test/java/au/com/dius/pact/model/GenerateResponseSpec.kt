package au.com.dius.pact.model

import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.generators.RandomIntGenerator
import au.com.dius.pact.model.generators.RandomStringGenerator
import au.com.dius.pact.model.generators.UuidGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kotlintest.matchers.between
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldHave
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.StringSpec

class GenerateResponseSpec : StringSpec() {

    private val generators: Generators = Generators().apply {
        addGenerator(Category.STATUS, generator = RandomIntGenerator(400, 499))
        addGenerator(Category.HEADER, "A", UuidGenerator())
        addGenerator(Category.BODY, "$.a", RandomStringGenerator())
    }
    private val response: Response = Response(generators = generators)

    init {
        "applies status generator for status to the copy of the response" {
            // given:
            response.status = 200

            // when:
            val generated = response.generateResponse()

            // then:
            generated.status shouldHave between(400, 499)
        }

        "applies header generator for headers to the copy of the response" {
            // given:
            response.headers = mapOf("A" to "a", "B" to "b")

            // when:
            val generated = response.generateResponse()

            // then:
            generated.headers["A"] shouldNotBe "a"
            generated.headers["B"] shouldBe "b"
        }

        "applies body generators for body values to the copy of the response" {
            // given:
            val body = mapOf("a" to "A", "b" to "B")
            response.body = OptionalBody.body(Gson().toJson(body).toString())

            // when:
            val generated = response.generateResponse()
            val typeOfHashMap = object : TypeToken<Map<String, String>>() { }.type

            val generatedBody = Gson().fromJson<Map<String, String>>((generated.body as OptionalBody.StringBody).unwrap(), typeOfHashMap)

            // then:
            generatedBody["a"] shouldNotBe "A"
            generatedBody["b"] shouldBe "B"
        }
    }
}