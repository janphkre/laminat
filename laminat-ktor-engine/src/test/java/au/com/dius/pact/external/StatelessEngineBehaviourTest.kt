package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class StatelessEngineBehaviourTest : AbstractRequestTest() {

    private lateinit var mockEngineBehaviour: StatelessPactBehaviour

    @Before
    fun setup() {
        mockEngineBehaviour = StatelessPactBehaviour()
        mockEngineBehaviour.observeMatches { incomingRequest, requestMatch ->
            println(requestMatch)
        }
    }

    @After
    fun teardown() {
        mockEngineBehaviour.teardown()
    }

    private val testPost by lazy {
        ConsumerPactBuilder("TestConsumer").hasPactWith("TestProducer")
            .uponReceiving("POST testRequest")
            .method("POST")
            .path("/test/path")
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
    fun statelessPactBehaviour_PostRequest_MatchingCorrectly() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val incomingRequest = createIncomingRequest(request)
        mockEngineBehaviour.addPact(testPost)

        val response = mockEngineBehaviour.createMatch(incomingRequest)
        Assert.assertTrue(response is RequestMatch.FullRequestMatch)
        val responseBody = String(response.interaction!!.response.body.asBinary(Charsets.UTF_8))
        Assert.assertEquals("{\"regex3\":\"12345\",\"regex4\":\"abc\"}", responseBody)
        Assert.assertEquals(200, response.interaction!!.response.status)
    }

    @Test
    fun statelessPactBehaviour_PostRequestEmpty_NotMatching() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val incomingRequest = createIncomingRequest(request)

        val response = mockEngineBehaviour.createMatch(incomingRequest)
        Assert.assertTrue(response is RequestMatch.RequestMismatch)
    }

    @Test
    fun statelessPactBehaviour_PostRequestUnmatched_PartialMatching() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\"}".toByteArray()

        val incomingRequest = createIncomingRequest(request)
        mockEngineBehaviour.addPact(testPost)

        val response = mockEngineBehaviour.createMatch(incomingRequest)
        Assert.assertTrue(response is RequestMatch.PartialRequestMatch)
        val responseBody = String(response.interaction!!.response.body.asBinary(Charsets.UTF_8))
        Assert.assertEquals("{\"regex3\":\"12345\",\"regex4\":\"abc\"}", responseBody)
    }
}