package au.com.dius.pact.consumer;

import org.w3c.dom.Document;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponseInteraction;

public class ConsumerPactBuilder {

    private String consumerName;
    private List<RequestResponseInteraction> interactions = new ArrayList<RequestResponseInteraction>();

    public ConsumerPactBuilder(String consumer) {
        this.consumerName = consumer;
    }

    /**
     * Name the consumer of the au.com.dius.pact
     * @param consumer Consumer name
     */
    public static ConsumerPactBuilder consumer(String consumer) {
        return new ConsumerPactBuilder(consumer);
    }

    /**
     * Name the provider that the consumer has a au.com.dius.pact with
     * @param provider provider name
     */
    public PactDslWithProvider hasPactWith(String provider) {
        return new PactDslWithProvider(this, provider);
    }

    public static PactDslJsonBody jsonBody() {
        return new PactDslJsonBody();
    }

    public static String xmlToString(Document body) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(body);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

    /**
     * Returns the name of the consumer
     * @return consumer name
     */
    public String getConsumerName() {
        return consumerName;
    }

    public List<RequestResponseInteraction> getInteractions() {
        return interactions;
    }
}
