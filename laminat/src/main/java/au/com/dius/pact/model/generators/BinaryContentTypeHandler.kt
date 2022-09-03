package au.com.dius.pact.model.generators

import au.com.dius.pact.model.OptionalBody

object BinaryContentTypeHandler : ContentTypeHandler {
    override fun generateBody(value: String, generators: Map<String, Generator>): OptionalBody {
        val bodyBinary = QueryResult(ByteArray(0))
        generators.forEach { (key, generator) ->
            applyKey(bodyBinary, key, generator)
        }
        return OptionalBody.body(bodyBinary.value)
    }

    private fun applyKey(body: QueryResult<ByteArray>, key: String, generator: Generator) {
        // Ignore key in a binary generator
        body.value = generator.generate(body.value) as ByteArray
    }
}