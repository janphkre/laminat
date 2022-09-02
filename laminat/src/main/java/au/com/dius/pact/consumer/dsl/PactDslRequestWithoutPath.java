package au.com.dius.pact.consumer.dsl;

import com.mifmif.common.regex.Generex;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.matchers.ByteArrayExtKt;
import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.PactReader;
import au.com.dius.pact.model.generators.Generators;
import au.com.dius.pact.model.matchingrules.MatchingRules;
import au.com.dius.pact.model.matchingrules.RegexMatcher;

public class PactDslRequestWithoutPath {
    private final ConsumerPactBuilder consumerPactBuilder;
    private final PactDslWithState pactDslWithState;
    private final String description;
    private String requestMethod;
    private Map<String, String> requestHeaders = new HashMap<>();
    private Map<String, List<String>> query = new HashMap<>();
    private OptionalBody requestBody = OptionalBody.missing();
    private final MatchingRules requestMatchers = new MatchingRules();
    private final Generators requestGenerators = new Generators();
    private final String consumerName;
    private final String providerName;

    public PactDslRequestWithoutPath(ConsumerPactBuilder consumerPactBuilder,
                                     PactDslWithState pactDslWithState,
                                     String description) {
        this.consumerPactBuilder = consumerPactBuilder;
        this.pactDslWithState = pactDslWithState;
        this.description = description;
        this.consumerName = pactDslWithState.consumerName;
        this.providerName = pactDslWithState.providerName;
    }

    /**
     * The HTTP method for the request
     *
     * @param method Valid HTTP method
     */
    public PactDslRequestWithoutPath method(String method) {
        requestMethod = method;
        return this;
    }

    /**
     * Headers to be included in the request
     *
     * @param headers Key-value pairs
     */
    public PactDslRequestWithoutPath headers(Map<String, String> headers) {
        requestHeaders = new HashMap<String, String>(headers);
        return this;
    }

    /**
     * Headers to be included in the request
     *
     * @param firstHeaderName      The name of the first header
     * @param firstHeaderValue     The value of the first header
     * @param headerNameValuePairs Additional headers in name-value pairs.
     */
    public PactDslRequestWithoutPath headers(String firstHeaderName, String firstHeaderValue, String... headerNameValuePairs) {
        if (headerNameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Pair key value should be provided, but there is one key without value.");
        }
        requestHeaders.put(firstHeaderName, firstHeaderValue);

        for (int i = 0; i < headerNameValuePairs.length; i+=2) {
            requestHeaders.put(headerNameValuePairs[i], headerNameValuePairs[i+1]);
        }

        return this;
    }

    /**
     * The query string for the request
     *
     * @param query query string
     */
    public PactDslRequestWithoutPath query(String query) {
        this.query = PactReader.INSTANCE.queryStringToMap(query, false);
        return this;
    }

    /**
     * The string body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(String body) {
        requestBody = OptionalBody.body(body);
        return this;
    }

    /**
     * The string body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(String body, String mimeType) {
        requestHeaders.put(ContentType.CONTENT_TYPE, mimeType);
        return body(body);
    }

    /**
     * The string body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(String body, ContentType mimeType) {
        return body(body, mimeType.toString());
    }

    /**
     * The binary body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(byte[] body) {
        requestBody = OptionalBody.body(body);
        return this;
    }

    /**
     * The binary body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(byte[] body, String mimeType) {
        requestHeaders.put(ContentType.CONTENT_TYPE, mimeType);
        return body(body);
    }

    /**
     * The binary body of the request
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath body(byte[] body, ContentType mimeType) {
        return body(body, mimeType.toString());
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using {@link QuoteUtil} to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath bodyWithSingleQuotes(String body) {
        if (body != null) {
            body = QuoteUtil.convert(body);
        }
        return body(body);
    }


    /**
     * The body of the request with possible single quotes as delimiters
     * and using {@link QuoteUtil} to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath bodyWithSingleQuotes(String body, String mimeType) {
        if (body != null) {
            body = QuoteUtil.convert(body);
        }
        return body(body, mimeType);
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using {@link QuoteUtil} to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    public PactDslRequestWithoutPath bodyWithSingleQuotes(String body, ContentType mimeType) {
        if (body != null) {
            body = QuoteUtil.convert(body);
        }
        return body(body, mimeType);
    }

    /**
     * The body of the request
     *
     * @param body Request body in JSON form
     */
    public PactDslRequestWithoutPath body(JSONObject body) {
        body(body.toString());
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        }
        return this;
    }

    /**
     * The body of the request
     *
     * @param body Built using the Pact body DSL
     */
    public PactDslRequestWithoutPath body(DslPart body) {
        DslPart parent = body.close();
        requestMatchers.addCategory(parent.matchers);
        requestGenerators.addGenerators(parent.generators);
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        }
        return body(parent.toString());
    }

    /**
     * The body of the request
     *
     * @param body XML Document
     */
    public PactDslRequestWithoutPath body(Document body) throws TransformerException {
        if (!requestHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            requestHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_XML.toString());
        }
        return body(ConsumerPactBuilder.xmlToString(body));
    }

    /**
     * The path of the request
     *
     * @param path string path
     */
    public PactDslRequestWithPath path(String path) {
        return new PactDslRequestWithPath(consumerPactBuilder, consumerName, providerName, pactDslWithState.state, description, path,
                requestMethod, requestHeaders, query, requestBody, requestMatchers, requestGenerators);
    }

    /**
     * The path of the request. This will generate a random path to use when generating requests
     *
     * @param pathRegex string path regular expression to match with
     */
    public PactDslRequestWithPath matchPath(String pathRegex) {
        return matchPath(pathRegex, new Generex(pathRegex).random());
    }

    /**
     * The path of the request
     *
     * @param path      string path to use when generating requests
     * @param pathRegex regular expression to use to match paths
     */
    public PactDslRequestWithPath matchPath(String pathRegex, String path) {
        requestMatchers.addCategory("path").addRule(new RegexMatcher(pathRegex));
        return new PactDslRequestWithPath(
                consumerPactBuilder,
                consumerName,
                providerName,
                pactDslWithState.state,
                description,
                path,
                requestMethod,
                requestHeaders,
                query,
                requestBody,
                requestMatchers,
                requestGenerators
        );
    }
}
