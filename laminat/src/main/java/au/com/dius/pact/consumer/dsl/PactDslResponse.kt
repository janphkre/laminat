package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.ConsumerPactBuilder.Companion.xmlToString
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.OptionalBody.Companion.missing
import au.com.dius.pact.model.OptionalBody.Companion.nullBody
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.Response
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher
import com.mifmif.common.regex.Generex
import org.apache.http.entity.ContentType
import org.json.JSONObject
import org.w3c.dom.Document
import javax.xml.transform.TransformerException

@Suppress("MemberVisibilityCanBePrivate", "unused")
class PactDslResponse(private val consumerPactBuilder: ConsumerPactBuilder, private val request: PactDslRequestWithPath) {
    private var responseStatus = 200
    private val responseHeaders: MutableMap<String, String> = HashMap()
    private var responseBody = missing()
    private val responseMatchers = MatchingRules()
    private val responseGenerators = Generators()

    /**
     * Response status code
     *
     * @param status HTTP status code
     */
    fun status(status: Int): PactDslResponse {
        responseStatus = status
        return this
    }

    /**
     * Response headers to return
     *
     * Provide the headers you want to validate, other headers will be ignored.
     *
     * @param headers key-value pairs of headers
     */
    fun headers(headers: Map<String, String>?): PactDslResponse {
        responseHeaders.putAll(headers!!)
        return this
    }

    /**
     * Response body to return
     *
     * @param body Response body in string form
     */
    fun body(body: String?): PactDslResponse {
        responseBody = OptionalBody.body(body)
        return this
    }

    /**
     * Response body to return
     *
     * @param body body in string form
     * @param mimeType the Content-Type response header value
     */
    fun body(body: String?, mimeType: String): PactDslResponse {
        responseBody = OptionalBody.body(body)
        responseHeaders[ContentType.CONTENT_TYPE] = mimeType
        return this
    }

    /**
     * Response body to return
     *
     * @param body body in string form
     * @param mimeType the Content-Type response header value
     */
    fun body(body: String?, mimeType: ContentType): PactDslResponse {
        return body(body, mimeType.toString())
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?): PactDslResponse {
        return body(body?.let { QuoteUtil.convert(it) })
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     * @param mimeType the Content-Type response header value
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: String): PactDslResponse {
        return body(body?.let { QuoteUtil.convert(it) }, mimeType)
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     * @param mimeType the Content-Type response header value
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: ContentType): PactDslResponse {
        return bodyWithSingleQuotes(body, mimeType.toString())
    }

    /**
     * Response body to return
     *
     * @param body Response body in JSON form
     */
    fun body(body: JSONObject): PactDslResponse {
        responseBody = OptionalBody.body(body.toString())
        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_JSON.toString()
        }
        return this
    }

    /**
     * Response body to return
     *
     * @param body Response body built using the Pact body DSL
     */
    fun body(body: DslPart): PactDslResponse {
        val parent = body.close()
        if (parent is PactDslJsonRootValue) {
            parent.isEncodeJson = true
        }
        responseMatchers.addCategory(parent.matchers)
        responseGenerators.addGenerators(parent.generators)
        responseBody = if (parent.body != null) {
            OptionalBody.body(parent.body.toString())
        } else {
            nullBody()
        }
        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_JSON.toString()
        }
        return this
    }

    /**
     * Response body to return
     *
     * @param body Response body as an XML Document
     */
    @Throws(TransformerException::class)
    fun body(body: Document?): PactDslResponse {
        responseBody = OptionalBody.body(xmlToString(body))
        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_XML.toString()
        }
        return this
    }

    /**
     * Match a response header.
     *
     * @param header Header to match
     * @param regexp Regular expression to match
     * @param headerExample Example value to use
     */
    @JvmOverloads
    fun matchHeader(header: String, regexp: String?, headerExample: String = Generex(regexp).random()): PactDslResponse {
        responseMatchers.addCategory("header").addRule(header, RegexMatcher(regexp!!))
        responseHeaders[header] = headerExample
        return this
    }

    fun toResponse(): Response {
        return Response(responseStatus, responseHeaders, responseBody, responseMatchers, responseGenerators)
    }

    private fun addInteraction() {
        consumerPactBuilder.interactions.add(
            RequestResponseInteraction(
                request.description,
                request.state,
                Request(
                    request.requestMethod, request.path, request.query,
                    request.requestHeaders, request.requestBody, request.requestMatchers, request.requestGenerators
                ),
                toResponse()
            )
        )
    }

    /**
     * Terminates the DSL and builds a au.com.dius.pact to represent the interactions
     */
    fun toPact(): RequestResponsePact {
        addInteraction()
        return RequestResponsePact(request.provider, request.consumer, consumerPactBuilder.interactions)
    }

    /**
     * Description of the request that is expected to be received
     *
     * @param description request description
     */
    fun uponReceiving(description: String?): PactDslRequestWithPath {
        addInteraction()
        return PactDslRequestWithPath(consumerPactBuilder, request, description!!)
    }

    /**
     * Adds a provider state to this interaction
     * @param state Description of the state
     */
    fun given(state: String): PactDslWithState {
        addInteraction()
        return PactDslWithState(
            consumerPactBuilder, request.consumer.name, request.provider.name,
            ProviderState(state)
        )
    }

    /**
     * Adds a provider state to this interaction
     * @param state Description of the state
     * @param params Data parameters for this state
     */
    fun given(state: String, params: Map<String, Any>): PactDslWithState {
        addInteraction()
        return PactDslWithState(
            consumerPactBuilder, request.consumer.name, request.provider.name,
            ProviderState(state, params)
        )
    }
}