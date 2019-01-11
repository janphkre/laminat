package au.com.dius.pact.model

import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules
import com.google.gson.JsonParser

class Request(
    val method: String = DEFAULT_METHOD,
    val path: String = DEFAULT_PATH,
    val query: Map<String, List<String>> = emptyMap(),
    override var headers: Map<String, String> = emptyMap(),
    override val body: OptionalBody = OptionalBody.missing(),
    override val matchingRules: MatchingRules = MatchingRules(),
    val generators: Generators = Generators()
) : HttpPart(), Comparable<Request> {

    override fun compareTo(other: Request): Int {
        return if(equals(other)) 0 else 1
    }

    fun copy(): Request {
        return Request(method, path,
            HashMap<String, List<String>>(query),
            HashMap<String, String>(headers),
            body,
            matchingRules.copy(),
            generators.copy())
    }

    override fun toString(): String {
        return "\tmethod: $method\n\tpath: $path\n\tquery: $query\n\theaders: $headers\n\tmatchers: $matchingRules\n\t" +
                "generators: $generators\n\tbody: $body"
    }

    fun headersWithoutCookie(): Map<String, String> {
        return headers.filter { mapEntry -> mapEntry.key.toLowerCase() != COOKIE_KEY }
    }

    fun cookie(): List<String> {
        val cookieEntry = headers[COOKIE_KEY] ?: return emptyList()
        return cookieEntry.split(';').map { it.trim() }
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Request) {
            return false
        }
        return this. method == other.method &&
                this.path == other.path &&
                this.query.keys.size == other.query.keys.size &&
                this.query.keys.containsAll(other.query.keys) &&
                matchesBody(other)
    }

    fun matchesBody(other: Request): Boolean {
        if(mimeType() != other.mimeType()) {
            return false
        }
        if(jsonBody() && other.jsonBody()) {
            try {
                val jsonModel = jsonParser.parse(body.value)
                val otherJsonModel = jsonParser.parse(other.body.value)
                return jsonModel == otherJsonModel
            } catch(e: Exception) { }
        }
        return body.equals(other.body)
    }

    companion object {
        const val COOKIE_KEY = "cookie"
        const val DEFAULT_METHOD = "GET"
        const val DEFAULT_PATH = "/"
        val jsonParser = JsonParser()

        fun fromMap(map: Map<*, *>): Request {
            return Request(
            method = (map["method"] ?: DEFAULT_METHOD) as String,
            path = (map["path"] ?: DEFAULT_PATH) as String,
            query = map["query"] as? Map<String, List<String>> ?: emptyMap(),
            headers = map["headers"] as? Map<String, String> ?: emptyMap(),
            body = if(map.containsKey("body")) OptionalBody.body(map["body"] as String?) else OptionalBody.missing(),
            matchingRules = MatchingRules(),//TODO: MatchingRules.fromMap(map["matchingRules"]),
            generators = Generators.fromMap(map["generators"] as? Map<String, Map<String, Any>>)
            )
        }
    }
}