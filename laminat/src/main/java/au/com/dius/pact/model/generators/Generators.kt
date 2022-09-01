package au.com.dius.pact.model.generators

import au.com.dius.pact.matchers.MatchingConfig
import au.com.dius.pact.model.InvalidPactException
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.PactSpecVersion
import java.util.Locale
import org.apache.http.entity.ContentType

data class Generators(val categories: MutableMap<Category, MutableMap<String, Generator>> = HashMap()) {

    @JvmOverloads
    fun addGenerator(category: Category, key: String? = "", generator: Generator): Generators {
        if (categories.containsKey(category) && categories[category] != null) {
            categories[category]?.put(key ?: "", generator)
        } else {
            categories[category] = mutableMapOf((key ?: "") to generator)
        }
        return this
    }

    @JvmOverloads
    fun addGenerators(generators: Generators, keyPrefix: String = ""): Generators {
        generators.categories.forEach { (category, map) ->
            map.forEach { (key, generator) ->
                addGenerator(category, keyPrefix + key, generator)
            }
        }
        return this
    }

    fun addCategory(category: Category): Generators {
        if (!categories.containsKey(category)) {
            categories[category] = mutableMapOf()
        }
        return this
    }

    fun applyGenerator(category: Category, closure: (String, Generator?) -> Unit) {
        if (categories.containsKey(category) && categories[category] != null) {
            val categoryValues = categories[category]
            if (categoryValues != null) {
                for ((key, value) in categoryValues) {
                    closure.invoke(key, value)
                }
            }
        }
    }

    fun applyBodyGenerators(body: OptionalBody, contentType: String): OptionalBody {
        return when (body) {
            OptionalBody.EmptyBody, OptionalBody.MissingBody, OptionalBody.NullBody -> body
            is OptionalBody.StringBody -> when {
                MatchingConfig.isJson(contentType) -> processBody(body.unwrap(), ContentType.APPLICATION_JSON.mimeType)
                MatchingConfig.isXml(contentType) -> processBody(body.unwrap(), ContentType.APPLICATION_XML.mimeType)
                else -> body
            }
            is OptionalBody.BinaryBody -> body // TODO GENERATE BINARY BODY!
        }
    }

    private fun processBody(value: String, contentType: String): OptionalBody {
        val handler = GeneratorsConfig.lookupContentTypeHandler(contentType)
        return handler?.processBody(value) { body: QueryResult ->
            applyGenerator(Category.BODY) { key: String, generator: Generator? ->
                if (generator != null) {
                    handler.applyKey(body, key, generator)
                }
            }
        } ?: OptionalBody.body(value)
    }

    /**
     * If there are no generators
     */
    fun isEmpty() = categories.isEmpty()

    /**
     * If there are generators
     */
    fun isNotEmpty() = categories.isNotEmpty()

    fun toMap(pactSpecVersion: PactSpecVersion): Map<String, Any> {
        if (pactSpecVersion < PactSpecVersion.V3) {
            throw InvalidPactException("Generators are only supported with au.com.dius.pact specification version 3+")
        }
        return categories.entries.associate { (key, value) ->
            when (key) {
                Category.METHOD, Category.PATH, Category.STATUS -> key.name.lowercase(Locale.ROOT) to value[""]!!.toMap(
                    pactSpecVersion
                )
                else -> key.name.lowercase(Locale.ROOT) to value.entries.associate { (genKey, generator) ->
                    genKey to generator.toMap(pactSpecVersion)
                }
            }
        }
    }

    fun applyRootPrefix(prefix: String) {
        categories.keys.forEach { category ->
            categories[category] = categories[category]!!.mapKeys { e ->
                if (e.key.startsWith(prefix)) {
                    e.key
                } else {
                    prefix + e.key
                }
            }.toMutableMap()
        }
    }
}