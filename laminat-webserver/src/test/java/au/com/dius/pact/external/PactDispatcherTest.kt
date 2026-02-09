package au.com.dius.pact.external

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.RequestResponseInteraction
import org.junit.Assert
import org.junit.Test

class PactDispatcherTest : AbstractRequestTest() {

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
                    .equalTo("stringType", "agbdfbdf")
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
    fun pactDispatcher_PostRequest_MatchingCorrectly() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"stringType\": \"agbdfbdf\", \"decimal1\": 50.99234}".toByteArray()

        val dispatcher = PactDispatcher(false, 998)
        val incomingRequest = getRecordedRequest(request)

        @Suppress("UNCHECKED_CAST")
        dispatcher.setInteractions(testPost.interactions as List<RequestResponseInteraction>)

        val response = dispatcher.dispatch(incomingRequest)
        val responseBody = String(response.getBody()?.readByteArray() ?: ByteArray(0))
        Assert.assertEquals("{\"regex3\":\"12345\",\"regex4\":\"abc\"}", responseBody)
        Assert.assertEquals("HTTP/1.1 200 OK", response.status)
    }

    @Test
    fun pactDispatcher_PostRequestEmpty_NotMatching() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val dispatcher = PactDispatcher(false, 998)
        val incomingRequest = getRecordedRequest(request)

        dispatcher.setInteractions(emptyList())

        val response = dispatcher.dispatch(incomingRequest)
        Assert.assertEquals("HTTP/1.1 998 PactError", response.status)
        val responseBody = String(response.getBody()?.readByteArray() ?: ByteArray(0))
        Assert.assertEquals("Failed to match request at all! Best match was with null:\nnull", responseBody)
    }

    @Test
    fun pactDispatcher_PostRequestUnmatched_PartialMatching() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\"}".toByteArray()

        val dispatcher = PactDispatcher(false, 998)
        val incomingRequest = getRecordedRequest(request)

        @Suppress("UNCHECKED_CAST")
        dispatcher.setInteractions(testPost.interactions as List<RequestResponseInteraction>)

        val response = dispatcher.dispatch(incomingRequest)
        Assert.assertEquals("HTTP/1.1 998 PactError", response.status)
        val responseBody = String(response.getBody()?.readByteArray() ?: ByteArray(0))
        Assert.assertTrue(responseBody.startsWith("Partially matched None_POST testRequest:"))
    }

    @Test
    fun pactDispatcher_PostRequestIncorrectData_PartialMatching() {
        val request = "{ \"regex1\": \"123456789\", \"regex2\": \"abcd\", \"stringType\": \"abcd\", \"decimal1\": 50.99234}".toByteArray()

        val dispatcher = PactDispatcher(false, 998)
        val incomingRequest = getRecordedRequest(request)

        @Suppress("UNCHECKED_CAST")
        dispatcher.setInteractions(testPost.interactions as List<RequestResponseInteraction>)

        val response = dispatcher.dispatch(incomingRequest)
        Assert.assertEquals("HTTP/1.1 998 PactError", response.status)
        val responseBody = String(response.getBody()?.readByteArray() ?: ByteArray(0))
        Assert.assertTrue(responseBody.startsWith("Partially matched None_POST testRequest:"))
    }
}