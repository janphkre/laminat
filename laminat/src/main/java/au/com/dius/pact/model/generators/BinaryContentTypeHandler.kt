package au.com.dius.pact.model.generators

import au.com.dius.pact.model.OptionalBody

object BinaryContentTypeHandler : ContentTypeHandler {
    override fun processBody(value: String, fn: (QueryResult) -> Unit): OptionalBody {
        val bodyBinary = QueryResult(ByteArray(0))
        fn.invoke(bodyBinary)
        return OptionalBody.body(bodyBinary.value as ByteArray)
    }

    override fun applyKey(body: QueryResult, key: String, generator: Generator) {
        // Ignore key in a binary generator
        body.value = generator.generate(body.value)
    }
}