package au.com.dius.pact.model.generators

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.util.toList
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.PathToken
import au.com.dius.pact.model.parsePath

object JsonContentTypeHandler : ContentTypeHandler {

    override fun generateBody(value: String, generators: Map<String, Generator>): OptionalBody {
        val bodyJson = QueryResult(Json.parse(value))
        generators.forEach { (key, generator) ->
            applyKey(bodyJson, key, generator)
        }
        return OptionalBody.body(Json.serialize(bodyJson.value))
    }

    private fun applyKey(body: QueryResult<Json>, key: String, generator: Generator) {
        val pathExp = parsePath(key)
        queryObjectGraph(pathExp.iterator(), body) { (value, valueKey, parent) ->
            @Suppress("UNCHECKED_CAST")
            val generatedValue = Json.convertToJson(generator.generate(value))
            when (parent) {
                is Json.Object -> parent[valueKey.toString()] = generatedValue
                is Json.Array -> parent[valueKey as Int] = generatedValue
                else -> {
                    body.value = generatedValue
                }
            }
        }
    }

    private fun queryObjectGraph(pathExp: Iterator<PathToken>, body: QueryResult<Json>, fn: (QueryResult<Json>) -> Unit) {
        var bodyCursor = body
        while (pathExp.hasNext()) {
            val token = pathExp.next()
            when (token) {
                is PathToken.Field -> {
                    val map = bodyCursor.value as? Json.Object
                    if (
                        map?.containsKey(token.name) == true
                    ) {
                        bodyCursor = QueryResult(map[token.name]!!, token.name, map)
                    } else {
                        return
                    }
                }
                is PathToken.Index -> {
                    val list = bodyCursor.value as? Json.Array
                    if (list != null && list.size > token.index) {
                        bodyCursor = QueryResult(list[token.index], token.index, list)
                    } else {
                        return
                    }
                }
                is PathToken.Star -> {
                    val map = bodyCursor.value as? Json.Object
                    if (map != null) {
                        val pathIterator = pathExp.iterator()
                        HashMap(map).forEach { (key, value) ->
                            queryObjectGraph(pathIterator.iterator(), QueryResult(value!!, key, map), fn)
                        }
                        return
                    } else {
                        return
                    }
                }
                is PathToken.StarIndex -> {
                    val list = bodyCursor.value as? Json.Array
                    if (list != null) {
                        val pathIterator = pathExp.toList()
                        list.forEachIndexed { index, item ->
                            queryObjectGraph(
                                pathIterator.iterator(),
                                QueryResult(item, index, list),
                                fn
                            )
                        }
                        return
                    } else {
                        return
                    }
                }
                is PathToken.Root -> {
                    bodyCursor = body
                }
            }
        }

        fn(bodyCursor)
    }
}