package au.com.dius.pact.model.generators

import au.com.dius.pact.model.OptionalBody

interface ContentTypeHandler {
    fun processBody(value: String, fn: (QueryResult) -> Unit): OptionalBody
    fun applyKey(body: QueryResult, key: String, generator: Generator)
}