package au.com.dius.pact.model.serialization

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.PactReader
import au.com.dius.pact.model.matchingrules.Category
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MatchingRulesSerialization

class RequestResponsePactV2Deserializer : RequestResponsePactV3Deserializer() {

    override fun mapQuery(json: Json): Map<String, List<String>> {
        val queryString = json.getValueOrNull<String>()
        return PactReader.queryStringToMap(queryString)
    }

    private data class IntermediateCategory(
        val categoryKey: String,
        val matchingRuleKey: String,
        val matchingRule: Json
    )

    override fun Json.toCategoryMap(): MutableMap<String, Category> {
        if (this is Json.Null) {
            return HashMap()
        }
        this as Json.Object
        return this.entries.map { (key, json) ->
            // Key structure: $.category.anything
            val keySplit = key.split('.')
            val offsetKey = keySplit.drop(2).joinToString(separator = ".", prefix = "$.")
            if (keySplit[1].startsWith(SerializationConstants.BODY_KEY, ignoreCase = true)) {
                IntermediateCategory(
                    SerializationConstants.BODY_KEY,
                    offsetKey,
                    json
                )
            } else if (keySplit[1].startsWith(SerializationConstants.HEADERS_KEY, ignoreCase = true)) {
                IntermediateCategory(
                    SerializationConstants.HEADERS_KEY,
                    offsetKey,
                    json
                )
            } else if (keySplit[1].startsWith(SerializationConstants.HEADER_KEY, ignoreCase = true)) {
                IntermediateCategory(
                    SerializationConstants.HEADERS_KEY,
                    offsetKey,
                    json
                )
            } else if (keySplit[1].startsWith(SerializationConstants.PATH_KEY, ignoreCase = true)) {
                IntermediateCategory(
                    SerializationConstants.PATH_KEY,
                    "",
                    json
                )
            } else {
                IntermediateCategory(
                    keySplit[1],
                    offsetKey,
                    json
                )
            }
        }.groupBy(keySelector = { intermediate ->
            intermediate.categoryKey
        }).mapValuesTo(LinkedHashMap()) { (key, intermediateList) ->

            MatchingRuleGroup().apply {
            }
            val matchingRules = intermediateList.associateTo(HashMap()) { intermediate ->
                val matchingRule = MatchingRulesSerialization.fromJson(intermediate.matchingRule)
                intermediate.matchingRuleKey to MatchingRuleGroup(mutableListOf(matchingRule))
            }

            Category(
                name = key,
                matchingRules = matchingRules.toMutableMap()
            )
        }
    }
}