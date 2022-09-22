package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValue
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.external.util.mapKeyValues
import au.com.dius.pact.model.serialization.RequestResponsePactV2Deserializer
import au.com.dius.pact.model.serialization.RequestResponsePactV3Deserializer
import au.com.dius.pact.model.serialization.SerializationConstants
import java.net.URLDecoder
import org.apache.http.Consts
import java.util.Locale

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
        val pactVersion = determineSpecVersion(pactJson)
        val deserializer = when (pactVersion) {
            PactSpecVersion.V2 -> RequestResponsePactV2Deserializer()
            PactSpecVersion.V3 -> RequestResponsePactV3Deserializer()
        }
        if (!deserializer.isValid(pactJson)) {
            throw InvalidPactException("Received invalid JSON for a pact. Can not be parsed to a pact in version ${pactVersion.value}!")
        }
        return deserializer.createPact(pactSource, pactJson)
    }

    fun transformJson(pactJson: Json): Json {
        val interactions = (pactJson[SerializationConstants.INTERACTIONS_KEY] as Json.Array)
        val transformedInteractions = interactions.mapTo(ArrayList<Json>(interactions.size)) { interaction ->
            interaction as Json.Object
            val result = interaction.mapKeyValues { (key, value) ->
                when(key) {
                    SerializationConstants.REQUEST_KEY -> Pair(key, transformRequestResponse(value))
                    SerializationConstants.RESPONSE_KEY -> Pair(key, transformRequestResponse(value))
                    SerializationConstants.PROVIDER_STATE_KEY_SNAKE_CASE -> Pair(SerializationConstants.PROVIDER_STATE_KEY, value)
                    else -> Pair(key, value)
                }
            }
            Json.Object(result)
        }
        (pactJson as Json.Object)[SerializationConstants.INTERACTIONS_KEY] = Json.Array(transformedInteractions)
        return pactJson
    }

    private fun transformRequestResponse(actionJson: Json): Json {
        actionJson as Json.Object
        val transformedAction = actionJson.mapKeyValues { (key, value) ->
            when(key) {
                SerializationConstants.MATCHING_RULES_REQUEST_KEY,
                SerializationConstants.MATCHING_RULES_RESPONSE_KEY -> Pair(SerializationConstants.MATCHING_RULES_KEY, value)
                SerializationConstants.METHOD_KEY -> Pair(key, Json.wrapInJson(value.getValueOrNull<String>()?.uppercase(Locale.ROOT) ?: value))
                else -> Pair(key, value)
            }
        }
        return Json.Object(transformedAction)
    }

    fun determineSpecVersion(json: Json): PactSpecVersion {
        var version: String? = null
        val metadata = ((json as? Json.Object)?.get("metadata") as? Json.Object) ?: return PactSpecVersion.V2
        if (metadata.containsKey("pactSpecificationVersion")) {
            version = metadata["pactSpecificationVersion"].getValue()
        } else if (metadata.containsKey("pactSpecification")) {
            val pactSpecification =  metadata["pactSpecification"]
            version = when(pactSpecification) {
                is Json.Object -> pactSpecification["version"].getValueOrNull()
                is Json.Primitive -> pactSpecification.getValue()
                else -> null
            }
        } else if (metadata.containsKey("pact-specification")) {
            val pactSpecification =  metadata["pact-specification"]
            version = when(pactSpecification) {
                is Json.Object -> pactSpecification["version"].getValueOrNull()
                is Json.Primitive -> pactSpecification.getValue()
                else -> null
            }
        }
        if (version == "3.0") {
            version = "3.0.0"
        }
        return PactSpecVersion.values().firstOrNull { it.value == version } ?: PactSpecVersion.V2
    }
}