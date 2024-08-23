package au.com.dius.pact.model.serialization

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSource

interface PactDeserializer {
    fun isValid(pactJson: Json): Boolean
    fun createPact(source: PactSource, pactJson: Json): Pact
}