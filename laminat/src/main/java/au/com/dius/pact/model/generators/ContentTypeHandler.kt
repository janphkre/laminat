package au.com.dius.pact.model.generators

import au.com.dius.pact.model.OptionalBody

interface ContentTypeHandler {
    fun generateBody(value: String, generators: Map<String, Generator>): OptionalBody
}