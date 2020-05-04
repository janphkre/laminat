package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.ConsumerPactBuilder.Companion.xmlToString
import au.com.dius.pact.model.Consumer
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.OptionalBody.Companion.missing
import au.com.dius.pact.model.PactReader.queryStringToMap
import au.com.dius.pact.model.Provider
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher
import com.mifmif.common.regex.Generex
import org.apache.http.entity.ContentType
import org.json.JSONObject
import org.w3c.dom.Document
import java.util.*
import javax.xml.transform.TransformerException

class PactDslRequestWithPath {
    private val consumerPactBuilder: ConsumerPactBuilder
    @JvmField
    var consumer: Consumer
    @JvmField
    var provider: Provider
    @JvmField
    var state: List<ProviderState>
    @JvmField
    var description: String
    var path = "/"
    @JvmField
    var requestMethod = "GET"
    @JvmField
    var requestHeaders: MutableMap<String, String> = HashMap()
    var query: Map<String, List<String>> = HashMap()
    @JvmField
    var requestBody = missing()
    @JvmField
    var requestMatchers = MatchingRules()
    @JvmField
    var requestGenerators = Generators()

    internal constructor(
        consumerPactBuilder: ConsumerPactBuilder,
        consumerName: String?,
        providerName: String?,
        state: List<ProviderState>,
        description: String,
        path: String,
        requestMethod: String,
        requestHeaders: MutableMap<String, String>,
        query: Map<String, List<String>>,
        requestBody: OptionalBody,
        requestMatchers: MatchingRules,
        requestGenerators: Generators
    ) {
        this.consumerPactBuilder = consumerPactBuilder
        this.requestMatchers = requestMatchers
        consumer = Consumer(consumerName!!)
        provider = Provider(providerName!!)
        this.state = state
        this.description = description
        this.path = path
        this.requestMethod = requestMethod
        this.requestHeaders = requestHeaders
        this.query = query
        this.requestBody = requestBody
        this.requestMatchers = requestMatchers
        this.requestGenerators = requestGenerators
    }

    internal constructor(
        consumerPactBuilder: ConsumerPactBuilder,
        existing: PactDslRequestWithPath,
        description: String
    ) {
        this.consumerPactBuilder = consumerPactBuilder
        consumer = existing.consumer
        provider = existing.provider
        state = existing.state
        this.description = description
    }

    /**
     * The HTTP method for the request
     *
     * @param method Valid HTTP method
     */
    fun method(method: String): PactDslRequestWithPath {
        requestMethod = method
        return this
    }

    /**
     * Headers to be included in the request
     *
     * @param firstHeaderName      The name of the first header
     * @param firstHeaderValue     The value of the first header
     * @param headerNameValuePairs Additional headers in name-value pairs.
     */
    fun headers(firstHeaderName: String, firstHeaderValue: String, vararg headerNameValuePairs: String): PactDslRequestWithPath {
        require(headerNameValuePairs.size % 2 == 0) { "Pair key value should be provided, but there is one key without value." }
        requestHeaders[firstHeaderName] = firstHeaderValue
        var i = 0
        while (i < headerNameValuePairs.size) {
            requestHeaders[headerNameValuePairs[i]] = headerNameValuePairs[i + 1]
            i += 2
        }
        return this
    }

    /**
     * Headers to be included in the request
     *
     * @param headers Key-value pairs
     */
    fun headers(headers: Map<String, String>?): PactDslRequestWithPath {
        requestHeaders.putAll(headers!!)
        return this
    }

    /**
     * The query string for the request
     *
     * @param query query string
     */
    fun query(query: String?): PactDslRequestWithPath {
        this.query = queryStringToMap(query, false)
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?): PactDslRequestWithPath {
        requestBody = OptionalBody.body(body)
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?, mimeType: String): PactDslRequestWithPath {
        requestBody = OptionalBody.body(body)
        requestHeaders[ContentType.CONTENT_TYPE] = mimeType
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?, mimeType: ContentType): PactDslRequestWithPath {
        return body(body, mimeType.toString())
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?): PactDslRequestWithPath {
        var body = body
        if (body != null) {
            body = QuoteUtil.convert(body)
        }
        return body(body)
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: String): PactDslRequestWithPath {
        var body = body
        if (body != null) {
            body = QuoteUtil.convert(body)
        }
        return body(body, mimeType)
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: ContentType): PactDslRequestWithPath {
        var body = body
        if (body != null) {
            body = QuoteUtil.convert(body)
        }
        return body(body, mimeType)
    }

    /**
     * The body of the request
     *
     * @param body Request body in JSON form
     */
    fun body(body: JSONObject): PactDslRequestWithPath {
        requestBody = OptionalBody.body(body.toString())
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_JSON.toString()
        }
        return this
    }

    /**
     * The body of the request
     *
     * @param body Built using the Pact body DSL
     */
    fun body(body: DslPart): PactDslRequestWithPath {
        val parent = body.close()
        requestMatchers.addCategory(parent.matchers)
        requestGenerators.addGenerators(parent.generators)
        requestBody = OptionalBody.body(parent.toString())
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_JSON.toString()
        }
        return this
    }

    /**
     * The body of the request
     *
     * @param body XML Document
     */
    @Throws(TransformerException::class)
    fun body(body: Document?): PactDslRequestWithPath {
        requestBody = OptionalBody.body(xmlToString(body))
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders[ContentType.CONTENT_TYPE] = ContentType.APPLICATION_XML.toString()
        }
        return this
    }

    /**
     * The path of the request
     *
     * @param path string path
     */
    fun path(path: String): PactDslRequestWithPath {
        this.path = path
        return this
    }
    /**
     * The path of the request
     *
     * @param path      string path to use when generating requests
     * @param pathRegex regular expression to use to match paths
     */
    /**
     * The path of the request. This will generate a random path to use when generating requests
     *
     * @param pathRegex string path regular expression to match with
     */
    @JvmOverloads
    fun matchPath(pathRegex: String?, path: String = Generex(pathRegex).random()): PactDslRequestWithPath {
        requestMatchers.addCategory("path").addRule(RegexMatcher(pathRegex!!))
        this.path = path
        return this
    }
    /**
     * Match a request header.
     *
     * @param header        Header to match
     * @param regex         Regular expression to match
     * @param headerExample Example value to use
     */
    /**
     * Match a request header. A random example header value will be generated from the provided regular expression.
     *
     * @param header Header to match
     * @param regex  Regular expression to match
     */
    @JvmOverloads
    fun matchHeader(header: String, regex: String?, headerExample: String = Generex(regex).random()): PactDslRequestWithPath {
        requestMatchers.addCategory("header").addRule(header, RegexMatcher(regex!!))
        requestHeaders[header] = headerExample
        return this
    }

    /**
     * Define the response to return
     */
    fun willRespondWith(): PactDslResponse {
        return PactDslResponse(consumerPactBuilder, this)
    }
    /**
     * Match a query parameter with a regex.
     * @param parameter Query parameter
     * @param regex Regular expression to match with
     * @param example Example value to use for the query parameter
     */
    /**
     * Match a query parameter with a regex. A random query parameter value will be generated from the regex.
     * @param parameter Query parameter
     * @param regex Regular expression to match with
     */
    @JvmOverloads
    fun matchQuery(parameter: String, regex: String?, example: String = Generex(regex).random()): PactDslRequestWithPath {
        requestMatchers.addCategory("query").addRule(parameter, RegexMatcher(regex!!))
        query.put(parameter, listOf(example))
        return this
    }
}