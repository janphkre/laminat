package au.com.dius.pact.consumer

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.requests.RequestResponseInteraction
import org.w3c.dom.Document
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Suppress("unused")
class ConsumerPactBuilder(val consumerName: String) {
    /**
     * Returns the name of the consumer
     * @return consumer name
     */
    val interactions: MutableList<RequestResponseInteraction> = ArrayList()

    /**
     * Name the provider that the consumer has a au.com.dius.pact with
     * @param provider provider name
     */
    fun hasPactWith(provider: String): PactDslWithProvider {
        return PactDslWithProvider(this, provider)
    }

    companion object {
        /**
         * Name the consumer of the au.com.dius.pact
         * @param consumer Consumer name
         */
        fun consumer(consumer: String): ConsumerPactBuilder {
            return ConsumerPactBuilder(consumer)
        }

        fun jsonBody(): PactDslJsonBody {
            return PactDslJsonBody()
        }

        @JvmStatic
        @Throws(TransformerException::class)
        fun xmlToString(body: Document?): String {
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val result = StreamResult(StringWriter())
            val source = DOMSource(body)
            transformer.transform(source, result)
            return result.writer.toString()
        }
    }
}