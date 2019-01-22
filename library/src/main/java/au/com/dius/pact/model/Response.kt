package au.com.dius.pact.model

import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules

class Response(
    var status: Int = DEFAULT_STATUS,
    override var headers: Map<String, String> = emptyMap(),
    override var body: OptionalBody = OptionalBody.missing(),
    override val matchingRules: MatchingRules = MatchingRules(),
    val generators: Generators = Generators()
): HttpPart() {

    override fun toString(): String {
        return "\tstatus: $status\n\theaders: $headers\n\tmatchers: $matchingRules\n\tgenerators: $generators\n\tbody: $body"
    }

    fun copy(): Response {
        return Response(
            status,
            HashMap<String, String>(headers),
            body,
            matchingRules.copy(),
            generators.copy()
        )
    }

    fun generateResponse(): Response {
        val r = copy()
        generators.applyGenerator(Category.STATUS) { key, g -> r.status = g?.generate(r.status) as? Int ?: DEFAULT_STATUS }
        val generatedHeaders = HashMap(headers)
        generators.applyGenerator(Category.HEADER) { key, g ->
            generatedHeaders[key] = g?.generate(r.headers[key]) as? String ?: ""
        }
        r.headers = generatedHeaders
        r.body = generators.applyBodyGenerators(r.body, mimeType())
        return r
    }

    companion object {
        const val DEFAULT_STATUS = 200

        fun fromMap(map: Map<*,*>): Response {
            return Response(
                status = (map["status"] ?: DEFAULT_STATUS) as Int,
                headers = map["headers"] as? Map<String, String> ?: emptyMap(),
                body = if(map.containsKey("body")) OptionalBody.body(map["body"] as String?) else OptionalBody.missing(),
                matchingRules = MatchingRules(),//TODO: MatchingRules.fromMap(map["matchingRules"]),
                generators = Generators.fromMap(map["generators"] as? Map<String, Map<String, Any>>)
            )
        }
    }
}