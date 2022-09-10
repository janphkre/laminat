package au.com.dius.pact.model.generators

import au.com.dius.pact.model.OptionalBody

object BinaryContentTypeHandler : ContentTypeHandler {
    override fun generateBody(value: String, generators: Map<String, Generator>): OptionalBody {
        val bodyBinary = QueryResult(ByteArray(0))
        generators.forEach { (_, generator) ->
            // Ignore key in a binary generator
            bodyBinary.value = generator.generate(bodyBinary.value) as ByteArray
        }
        return OptionalBody.body(bodyBinary.value)
    }
}