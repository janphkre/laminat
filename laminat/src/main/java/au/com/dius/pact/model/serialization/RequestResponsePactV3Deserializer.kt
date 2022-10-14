package au.com.dius.pact.model.serialization

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.getValue
import au.com.dius.pact.external.json.getValueOrNull
import au.com.dius.pact.model.Consumer
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Pact
import au.com.dius.pact.model.PactSource
import au.com.dius.pact.model.Provider
import au.com.dius.pact.model.ProviderState
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.RequestResponseInteraction
import au.com.dius.pact.model.RequestResponsePact
import au.com.dius.pact.model.Response
import au.com.dius.pact.model.generators.Category as GeneratorCategory
import au.com.dius.pact.model.generators.Generator
import au.com.dius.pact.model.generators.GeneratorSerialization
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.Category as MatchingCategory
import au.com.dius.pact.model.matchingrules.MatchingRuleGroup
import au.com.dius.pact.model.matchingrules.MatchingRules
import au.com.dius.pact.model.matchingrules.MatchingRulesSerialization
import au.com.dius.pact.model.matchingrules.RuleLogic
import java.util.EnumMap

open class RequestResponsePactV3Deserializer : PactDeserializer {

    override fun isValid(pactJson: Json): Boolean {
        return (pactJson as? Json.Object)?.containsKey(SerializationConstants.INTERACTIONS_KEY) ?: false
    }

    override fun createPact(source: PactSource, pactJson: Json): Pact {
        val provider = Provider.fromJson(pactJson[SerializationConstants.PROVIDER_KEY])
        val consumer = Consumer.fromJson(pactJson[SerializationConstants.CONSUMER_KEY])

        val interactions = (pactJson[SerializationConstants.INTERACTIONS_KEY] as Json.Array).map { interaction ->
            interaction as Json.Object
            val request = mapToRequest(interaction[SerializationConstants.REQUEST_KEY])
            val response = mapToResponse(interaction[SerializationConstants.RESPONSE_KEY])
            val providerStates = if (interaction.containsKey(SerializationConstants.PROVIDER_STATES_KEY)) {
                (interaction[SerializationConstants.PROVIDER_STATES_KEY] as Json.Array).map { ProviderState.fromJson(it) }
            } else if (interaction.containsKey(SerializationConstants.PROVIDER_STATE_KEY)) {
                listOf(ProviderState(interaction[SerializationConstants.PROVIDER_STATE_KEY].getValue()))
            } else {
                emptyList()
            }

            RequestResponseInteraction(interaction[SerializationConstants.DESCRIPTION_KEY].getValue(), providerStates, request, response)
        }

        val pact = RequestResponsePact(provider, consumer, interactions)
        pact.source = source
        return pact
    }

    internal fun mapToRequest(json: Json): Request {
        return Request.create(
            method = json[SerializationConstants.METHOD_KEY].getValueOrNull(),
            path = json[SerializationConstants.PATH_KEY].getValueOrNull(),
            headers = json[SerializationConstants.HEADERS_KEY].toStringMap(),
            query = mapQuery(json[SerializationConstants.QUERY_KEY]),
            body = json.extractBody(),
            matchingRules = MatchingRules(json[SerializationConstants.MATCHING_RULES_KEY].toCategoryMap()),
            generators = Generators(json[SerializationConstants.GENERATORS_KEY].toGeneratorsMap())
        )
    }

    internal fun mapToResponse(json: Json): Response {
        return Response.create(
            status = json[SerializationConstants.STATUS_KEY].getValueOrNull(),
            headers = json[SerializationConstants.HEADERS_KEY].toStringMap(),
            body = json.extractBody(),
            matchingRules = MatchingRules(json[SerializationConstants.MATCHING_RULES_KEY].toCategoryMap()),
            generators = Generators(json[SerializationConstants.GENERATORS_KEY].toGeneratorsMap())
        )
    }

    internal open fun mapQuery(json: Json): Map<String, List<String>> {
        return json.toStringListMap()
    }

    private fun Json.toStringMap(): Map<String, String> {
        if (this is Json.Null) {
            return emptyMap()
        }
        this as Json.Object
        return this.mapValues { it.value.getValue() }
    }

    private fun Json.toStringListMap(): Map<String, List<String>> {
        if (this is Json.Null) {
            return emptyMap()
        }
        this as Json.Object
        return this.mapValues { (it.value as Json.Array).map { arrayEntry -> arrayEntry.getValue() } }
    }

    private fun Json.extractBody(): OptionalBody {
        this as Json.Object
        if (!this.containsKey(SerializationConstants.BODY_KEY)) {
            return OptionalBody.missing()
        }
        val body = this[SerializationConstants.BODY_KEY]
        return when (body) {
            is Json.Null -> {
                OptionalBody.nullBody()
            }
            is Json.StringPrimitive -> {
                OptionalBody.body(body.getValue<String>())
            }
            else -> {
                OptionalBody.body(body.toString())
            }
        }
    }

    internal open fun Json.toCategoryMap(): MutableMap<String, MatchingCategory> {
        if (this is Json.Null) {
            return HashMap()
        }
        this as Json.Object
        return this.entries.associateTo(HashMap()) { (key, json) ->
            val adaptedKey = when (key) {
                SerializationConstants.HEADERS_KEY -> {
                    SerializationConstants.HEADER_KEY
                }
                else -> {
                    key
                }
            }
            val matchingCategory = MatchingCategory(
                name = adaptedKey,
                matchingRules = json.toMatchingRuleGroupsMap(adaptedKey)
            )
            adaptedKey to matchingCategory
        }
    }

    private fun Json.toMatchingRuleGroupsMap(prefixName: String): MutableMap<String, MatchingRuleGroup> {
        if (this is Json.Null) {
            return HashMap()
        }
        this as Json.Object
        if(this[SerializationConstants.MATCHERS_KEY] is Json.Array) {
            return mutableMapOf("" to this.toMatchingRuleGroup())
        }
        return this.entries.associateTo(HashMap()) { (key, json) ->
            val simplifiedKey = key.removePrefix("\$.$prefixName")
            simplifiedKey to json.toMatchingRuleGroup()
        }
    }

    private fun Json.toMatchingRuleGroup(): MatchingRuleGroup {
        this as Json.Object
        val array = this[SerializationConstants.MATCHERS_KEY] as? Json.Array ?: emptyList()
        val ruleLogicName = this[SerializationConstants.COMBINE_KEY].getValueOrNull<String>()
        return MatchingRuleGroup(
            rules = array.mapTo(ArrayList(array.size)) { MatchingRulesSerialization.fromJson(it) },
            ruleLogic = ruleLogicName?.let { deserializedName -> RuleLogic.values().firstOrNull { it.name == deserializedName } } ?: RuleLogic.AND
        )
    }

    private fun Json.toGeneratorsMap(): MutableMap<GeneratorCategory, MutableMap<String, Generator>> {
        if (this is Json.Null) {
            return EnumMap(GeneratorCategory::class.java)
        }
        this as Json.Object
        return this.entries.associateTo(EnumMap(GeneratorCategory::class.java)) { (key, json) ->
            val category = GeneratorCategory.values().first { it.name.equals(key, ignoreCase = true) }
            val generatorMap = when (category) {
                GeneratorCategory.METHOD, GeneratorCategory.PATH, GeneratorCategory.STATUS -> {
                    val generator = GeneratorSerialization.fromJson(json)
                    mutableMapOf("" to generator)
                }
                else -> {
                    json as Json.Object
                    json.entries.associateTo(HashMap()) { (key, generatorJson) ->
                        key to GeneratorSerialization.fromJson(generatorJson)
                    }
                }
            }
            category to generatorMap
        }
    }
}