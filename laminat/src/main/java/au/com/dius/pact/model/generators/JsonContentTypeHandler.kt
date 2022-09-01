package au.com.dius.pact.model.generators

import au.com.dius.pact.external.util.toList
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.PathToken
import au.com.dius.pact.model.parsePath
import com.google.gson.JsonParser

object JsonContentTypeHandler : ContentTypeHandler {
    override fun processBody(value: String, fn: (QueryResult) -> Unit): OptionalBody {
        val bodyJson = QueryResult(JsonParser.parseString(value))
        fn.invoke(bodyJson)
        return OptionalBody.body(bodyJson.value.toString())
    }

    override fun applyKey(body: QueryResult, key: String, generator: Generator) {
        val pathExp = parsePath(key)
        queryObjectGraph(pathExp.iterator(), body) { (value, valueKey, parent) ->
            @Suppress("UNCHECKED_CAST")
            when (parent) {
                is MutableMap<*, *> -> (parent as MutableMap<String, Any>)[valueKey.toString()] =
                    generator.generate(value)
                is MutableList<*> -> (parent as MutableList<Any>)[valueKey as Int] = generator.generate(value)
                else -> body.value = generator.generate(value)
            }
        }
    }

    private fun queryObjectGraph(pathExp: Iterator<PathToken>, body: QueryResult, fn: (QueryResult) -> Unit) {
        var bodyCursor = body
        while (pathExp.hasNext()) {
            val token = pathExp.next()
            when (token) {
                is PathToken.Field -> if (bodyCursor.value is Map<*, *> &&
                    (bodyCursor.value as Map<*, *>).containsKey(token.name)
                ) {
                    val map = bodyCursor.value as Map<*, *>
                    bodyCursor = QueryResult(map[token.name]!!, token.name, bodyCursor.value)
                } else {
                    return
                }
                is PathToken.Index -> if (bodyCursor.value is List<*> && (bodyCursor.value as List<*>).size > token.index) {
                    val list = bodyCursor.value as List<*>
                    bodyCursor = QueryResult(list[token.index]!!, token.index, bodyCursor.value)
                } else {
                    return
                }
                is PathToken.Star -> if (bodyCursor.value is MutableMap<*, *>) {
                    val map = bodyCursor.value as MutableMap<*, *>
                    val pathIterator = pathExp.iterator()
                    HashMap(map).forEach { (key, value) ->
                        queryObjectGraph(pathIterator.iterator(), QueryResult(value!!, key, map), fn)
                    }
                    return
                } else {
                    return
                }
                is PathToken.StarIndex -> if (bodyCursor.value is List<*>) {
                    val list = bodyCursor.value as List<*>
                    val pathIterator = pathExp.toList()
                    list.forEachIndexed { index, item ->
                        queryObjectGraph(
                            pathIterator.iterator(),
                            QueryResult(item!!, index, list),
                            fn
                        )
                    }
                    return
                } else {
                    return
                }
                is PathToken.Root -> {
                    bodyCursor = body
                }
            }
        }

        fn(bodyCursor)
    }
}