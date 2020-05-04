package au.com.dius.pact.consumer.dsl;

import com.mifmif.common.regex.Generex;

import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.model.OptionalBody;
import au.com.dius.pact.model.ProviderState;
import au.com.dius.pact.model.Request;
import au.com.dius.pact.model.RequestResponseInteraction;
import au.com.dius.pact.model.RequestResponsePact;
import au.com.dius.pact.model.Response;
import au.com.dius.pact.model.generators.Generators;
import au.com.dius.pact.model.matchingrules.MatchingRules;
import au.com.dius.pact.model.matchingrules.RegexMatcher;

public class PactDslResponse {
    private final ConsumerPactBuilder consumerPactBuilder;
    private PactDslRequestWithPath request;

    private int responseStatus = 200;
    private Map<String, String> responseHeaders = new HashMap<String, String>();
    private OptionalBody responseBody = OptionalBody.missing();
    private MatchingRules responseMatchers = new MatchingRules();
    private Generators responseGenerators = new Generators();

    public PactDslResponse(ConsumerPactBuilder consumerPactBuilder, PactDslRequestWithPath request) {
        this.consumerPactBuilder = consumerPactBuilder;
        this.request = request;
    }

    /**
     * Response status code
     *
     * @param status HTTP status code
     */
    public PactDslResponse status(int status) {
        this.responseStatus = status;
        return this;
    }

    /**
     * Response headers to return
     *
     * Provide the headers you want to validate, other headers will be ignored.
     *
     * @param headers key-value pairs of headers
     */
    public PactDslResponse headers(Map<String, String> headers) {
        this.responseHeaders.putAll(headers);
        return this;
    }

    /**
     * Response body to return
     *
     * @param body Response body in string form
     */
    public PactDslResponse body(String body) {
        this.responseBody = OptionalBody.body(body);
        return this;
    }

    /**
     * Response body to return
     *
     * @param body body in string form
     * @param mimeType the Content-Type response header value
     */
    public PactDslResponse body(String body, String mimeType) {
        responseBody = OptionalBody.body(body);
        responseHeaders.put(ContentType.CONTENT_TYPE, mimeType);
        return this;
    }

    /**
     * Response body to return
     *
     * @param body body in string form
     * @param mimeType the Content-Type response header value
     */
    public PactDslResponse body(String body, ContentType mimeType) {
        return body(body, mimeType.toString());
    }

    /**
     * The body of the request with possible single quotes as delimiters
     * and using {@link QuoteUtil} to convert single quotes to double quotes if required.
     *
     * @param body Request body in string form
     */
    public PactDslResponse bodyWithSingleQuotes(String body) {
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
     * @param mimeType the Content-Type response header value
     */
    public PactDslResponse bodyWithSingleQuotes(String body, String mimeType) {
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
     * @param mimeType the Content-Type response header value
     */
    public PactDslResponse bodyWithSingleQuotes(String body, ContentType mimeType) {
        return bodyWithSingleQuotes(body, mimeType.toString());
    }

    /**
     * Response body to return
     *
     * @param body Response body in JSON form
     */
    public PactDslResponse body(JSONObject body) {
        this.responseBody = OptionalBody.body(body.toString());
        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        }
        return this;
    }

    /**
     * Response body to return
     *
     * @param body Response body built using the Pact body DSL
     */
    public PactDslResponse body(DslPart body) {
        DslPart parent = body.close();

        if (parent instanceof PactDslJsonRootValue) {
          ((PactDslJsonRootValue)parent).setEncodeJson(true);
        }

        responseMatchers.addCategory(parent.getMatchers());
        responseGenerators.addGenerators(parent.generators);
        if (parent.getBody() != null) {
            responseBody = OptionalBody.body(parent.getBody().toString());
        } else {
            responseBody = OptionalBody.nullBody();
        }

        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        }
        return this;
    }

    /**
     * Response body to return
     *
     * @param body Response body as an XML Document
     */
    public PactDslResponse body(Document body) throws TransformerException {
        responseBody = OptionalBody.body(ConsumerPactBuilder.xmlToString(body));
        if (!responseHeaders.containsKey(ContentType.CONTENT_TYPE)) {
            responseHeaders.put(ContentType.CONTENT_TYPE, ContentType.APPLICATION_XML.toString());
        }
        return this;
    }

    /**
     * Match a response header. A random example header value will be generated from the provided regular expression.
     *
     * @param header Header to match
     * @param regexp Regular expression to match
     */
    public PactDslResponse matchHeader(String header, String regexp) {
        return matchHeader(header, regexp, new Generex(regexp).random());
    }

    /**
     * Match a response header.
     *
     * @param header        Header to match
     * @param regexp        Regular expression to match
     * @param headerExample Example value to use
     */
    public PactDslResponse matchHeader(String header, String regexp, String headerExample) {
        responseMatchers.addCategory("header").addRule(header, new RegexMatcher(regexp));
        responseHeaders.put(header, headerExample);
        return this;
    }

    public Response toResponse() {
        return new Response(responseStatus, responseHeaders, responseBody, responseMatchers, responseGenerators);
    }

    private void addInteraction() {
        consumerPactBuilder.interactions.add(new RequestResponseInteraction(
          request.description,
          request.state,
          new Request(request.requestMethod, request.path, request.query,
            request.requestHeaders, request.requestBody, request.requestMatchers, request.requestGenerators),
          toResponse()
        ));
    }

    /**
     * Terminates the DSL and builds a au.com.dius.pact to represent the interactions
     */
    public RequestResponsePact toPact() {
        addInteraction();
        return new RequestResponsePact(request.provider, request.consumer, consumerPactBuilder.interactions);
    }

    /**
     * Description of the request that is expected to be received
     *
     * @param description request description
     */
    public PactDslRequestWithPath uponReceiving(String description) {
        addInteraction();
        return new PactDslRequestWithPath(consumerPactBuilder, request, description);
    }

    /**
     * Adds a provider state to this interaction
     * @param state Description of the state
     */
    public PactDslWithState given(String state) {
        addInteraction();
        return new PactDslWithState(consumerPactBuilder, request.consumer.getName(), request.provider.getName(),
          new ProviderState(state));
    }

    /**
     * Adds a provider state to this interaction
     * @param state Description of the state
     * @param params Data parameters for this state
     */
    public PactDslWithState given(String state, Map<String, Object> params) {
      addInteraction();
      return new PactDslWithState(consumerPactBuilder, request.consumer.getName(), request.provider.getName(),
        new ProviderState(state, params));
    }
}
