package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import java.net.URLDecoder
import org.apache.http.Consts
import java.io.File
import java.net.URL

object PactReader {

    fun queryStringToMap(query: String?, decode: Boolean = true): Map<String, List<String>> {
        return query?.split('&')?.asSequence()?.map {
            it.split('=', limit = 2)
        }?.fold(HashMap()) { map, nameAndValue ->
            val name = if (decode) URLDecoder.decode(nameAndValue.first(), Consts.UTF_8.name()) else nameAndValue.first()
            val value = if (decode) URLDecoder.decode(nameAndValue.last(), Consts.UTF_8.name()) else nameAndValue.last()
            if (map.containsKey(name)) {
                (map[name]!! as MutableList<String>).add(value)
            } else {
                map[name] = mutableListOf(value)
            }
            map
        } ?: emptyMap()
    }

    fun readPact(source: PactReaderSource): Pact {
        val (pactJson, pactSource) = source.loadPact()
        return when (pactJson.grabSpecVersion()) {
            PactSpecVersion.V2 -> parseV2Pact(pactJson, pactSource)
            PactSpecVersion.V3 -> parseV3Pact(pactJson, pactSource)
        }
    }

    private fun parseV3Pact(pactJson: Json, pactSource: PactSource): Pact {
        val requestResponseDeserializer = RequestResponsePactV3Deserializer()
        if(!requestResponseDeserializer.isValid(pactJson)) {
            throw InvalidPactException("Received invalid JSON for a pact. Can not be parsed to a pact!")
        }
        return requestResponseDeserializer.createPact(pactSource, pactJson)
    }

    private fun parseV2Pact(pactJson: Json, pactSource: PactSource): Pact {
        TODO()
    }

    private fun Json.grabSpecVersion(): PactSpecVersion {
        var version: String? = null
        val metadata = ((this as? Json.Object)?.get("metadata") as? Json.Object) ?: return PactSpecVersion.V2
        if (metadata.containsKey("pactSpecificationVersion")) {
            version = metadata["pactSpecificationVersion"].getValue()
        } else if (metadata.containsKey("pactSpecification")) {
            version = metadata["pactSpecification"]["version"].getValue()
        } else if (metadata.containsKey("pact-specification")) {
            version = metadata["pact-specification"]["version"].getValue()
        }
        if (version == "3.0") {
            version = "3.0.0"
        }
        return PactSpecVersion.values().firstOrNull { it.value == version } ?: PactSpecVersion.V2
    }
}
