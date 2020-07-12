package au.com.dius.pact.consumer.dsl

import au.com.dius.pact.consumer.ConsumerPactBuilder
import au.com.dius.pact.consumer.ConsumerPactBuilder.Companion.xmlToString
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.RegexMatcher
import au.com.dius.pact.model.requests.OptionalBody
import au.com.dius.pact.model.requests.OptionalBody.Companion.missing
import au.com.dius.pact.util.PactReader.queryStringToMap
import com.mifmif.common.regex.Generex
import org.apache.http.entity.ContentType
import org.json.JSONObject
import org.w3c.dom.Document
import javax.xml.transform.TransformerException

@Suppress("MemberVisibilityCanBePrivate", "unused")
class PactDslRequestWithoutPath(
    private val consumerPactBuilder: ConsumerPactBuilder,
    private val pactDslWithState: PactDslWithState,
    private val description: String
) {
    private var requestMethod: String? = null
    private var requestHeaders: MutableMap<String, String> = HashMap()
    private var query: MutableMap<String, List<String>> = HashMap()
    private var requestBody = missing()
    private val requestMatchers = MatchingRules()
    private val requestGenerators = Generators()
    private val consumerName: String = pactDslWithState.consumerName
    private val providerName: String = pactDslWithState.providerName

    /**
     * The HTTP method for the request
     *
     * @param method Valid HTTP method
     */
    fun method(method: String?): PactDslRequestWithoutPath {
        requestMethod = method
        return this
    }

    /**
     * Headers to be included in the request
     *
     * @param headers Key-value pairs
     */
    fun headers(headers: Map<String, String>?): PactDslRequestWithoutPath {
        requestHeaders = HashMap(headers?.toMutableMap() ?: emptyMap())
        return this
    }

    /**
     * Headers to be included in the request
     *
     * @param firstHeaderName The name of the first header
     * @param firstHeaderValue The value of the first header
     * @param headerNameValuePairs Additional headers in name-value pairs.
     */
    fun headers(firstHeaderName: String, firstHeaderValue: String, vararg headerNameValuePairs: String): PactDslRequestWithoutPath {
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
     * The query string for the request
     *
     * @param query query string
     */
    fun query(query: String?): PactDslRequestWithoutPath {
        this.query = queryStringToMap(query, false)
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?): PactDslRequestWithoutPath {
        requestBody = OptionalBody.body(body)
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?, mimeType: String): PactDslRequestWithoutPath {
        requestBody = OptionalBody.body(body)
        requestHeaders[ContentType.CONTENT_TYPE] = mimeType
        return this
    }

    /**
     * The body of the request
     *
     * @param body Request body in string form
     */
    fun body(body: String?, mimeType: ContentType): PactDslRequestWithoutPath {
        return body(body, mimeType.toString())
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?): PactDslRequestWithoutPath {
        return body(body?.let { QuoteUtil.convert(it) })
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: String): PactDslRequestWithoutPath {
        return body(body?.let { QuoteUtil.convert(it) }, mimeType)
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using [QuoteUtil] to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    fun bodyWithSingleQuotes(body: String?, mimeType: ContentType): PactDslRequestWithoutPath {
        return body(body?.let { QuoteUtil.convert(it) }, mimeType)
    }

    /**
     * The body of the request
     *
     * @param body Request body in JSON form
     */
    fun body(body: JSONObject): PactDslRequestWithoutPath {
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
    fun body(body: DslPart): PactDslRequestWithoutPath {
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
    fun body(body: Document?): PactDslRequestWithoutPath {
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
    fun path(path: String?): PactDslRequestWithPath {
        return PactDslRequestWithPath(
            consumerPactBuilder, consumerName, providerName, pactDslWithState.state, description, path,
            requestMethod, requestHeaders, query, requestBody, requestMatchers, requestGenerators
        )
    }
    /**
     * The path of the request
     *
     * @param path string path to use when generating requests
     * @param pathRegex regular expression to use to match paths
     */
    @JvmOverloads
    fun matchPath(pathRegex: String?, path: String? = Generex(pathRegex).random()): PactDslRequestWithPath {
        requestMatchers.addCategory("path").addRule(RegexMatcher(pathRegex!!))
        return PactDslRequestWithPath(
            consumerPactBuilder, consumerName, providerName, pactDslWithState.state, description, path,
            requestMethod, requestHeaders, query, requestBody, requestMatchers, requestGenerators
        )
    }
}