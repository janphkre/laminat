package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.PactReaderSource
import au.com.dius.pact.model.PactReaderSpec
import au.com.dius.pact.model.PactSerializationConfig
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.PactWriter
import au.com.dius.pact.model.RequestResponseInteraction
import org.junit.Assert
import org.junit.Test
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter

class PactReaderTest {

    private val testPost by lazy {
        ConsumerPactBuilder("TestConsumer")
            .hasPactWith("TestProducer")
            .uponReceiving("POST testRequest")
            .method("POST")
            .path("/test/path")
            .matchPath("/.{4}/path")
            .headers(
                hashMapOf(
                    Pair("Content-Type", "application/json")
                )
            )
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex1", "\\d{8,9}", "123456789")
                    .stringMatcher("regex2", ".{4}", "abcd")
                    .decimalType("decimal1", 50.99234)
            )
            .willRespondWith()
            .status(200)
            .headers(hashMapOf(Pair("Content-Type", "application/json; charset=UTF-8")))
            .body(
                PactDslJsonBody()
                    .stringMatcher("regex3", "\\d{5,6}", "12345")
                    .stringMatcher("regex4", ".{3}", "abc")
            )
            .toPact()
    }

    @Test
    fun pactReader_pactV3WithPathMatcher_DeserializesCorrectly() {
        // given:
        val stringWriter = StringWriter()
        PactWriter.writePact(testPost, PrintWriter(stringWriter), PactSerializationConfig(PactSpecVersion.V3, null))
        val serializedPact = stringWriter.buffer.toString()
        // when:
        val readPact = PactReader.readPact(PactReaderSource.ReaderPactSource(StringReader(serializedPact)))

        // then:
        val expectedInteraction = testPost.interactions.first() as RequestResponseInteraction
        val actualInteraction = readPact.interactions.first() as RequestResponseInteraction

        compareInteractions(expectedInteraction, actualInteraction)
    }


    @Test
    fun pactReader_pactV2WithPathMatcher_DeserializesCorrectly() {
        // given:
        val stringWriter = StringWriter()
        PactWriter.writePact(testPost, PrintWriter(stringWriter), PactSerializationConfig(PactSpecVersion.V2, null))
        val serializedPact = stringWriter.buffer.toString()
        // when:
        val readPact = PactReader.readPact(PactReaderSource.ReaderPactSource(StringReader(serializedPact)))

        // then:
        val expectedInteraction = testPost.interactions.first() as RequestResponseInteraction
        val actualInteraction = readPact.interactions.first() as RequestResponseInteraction

        compareInteractions(expectedInteraction, actualInteraction)
    }

    private fun compareInteractions(expectedInteraction: RequestResponseInteraction, actualInteraction: RequestResponseInteraction) {
        Assert.assertEquals(
            expectedInteraction.providerStates,
            actualInteraction.providerStates
        )
        Assert.assertEquals(
            expectedInteraction.request.method,
            actualInteraction.request.method
        )
        Assert.assertEquals(
            expectedInteraction.request.body,
            actualInteraction.request.body
        )
        Assert.assertEquals(
            expectedInteraction.request.query,
            actualInteraction.request.query
        )
        Assert.assertEquals(
            expectedInteraction.request.path,
            actualInteraction.request.path
        )

        Assert.assertEquals(
            expectedInteraction.request.matchingRules.getCategories(),
            actualInteraction.request.matchingRules.getCategories()
        )
        expectedInteraction.request.matchingRules.getCategories().forEach { category ->
            Assert.assertEquals(
                "Category $category is not equals",
                expectedInteraction.request.matchingRules.getCategory(category),
                actualInteraction.request.matchingRules.getCategory(category)
            )
        }

        Assert.assertEquals(
            expectedInteraction.request.generators,
            actualInteraction.request.generators
        )
    }
}