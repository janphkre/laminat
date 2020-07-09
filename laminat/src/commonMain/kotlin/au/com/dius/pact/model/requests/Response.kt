package au.com.dius.pact.model.requests

import au.com.dius.pact.model.generators.Category
import au.com.dius.pact.model.generators.Generators
import au.com.dius.pact.model.matchingrules.MatchingRules

class Response(
    var status: Int = DEFAULT_STATUS,
    override var headers: Map<String, String> = emptyMap(),
    override var body: OptionalBody = OptionalBody.missing(),
    override val matchingRules: MatchingRules = MatchingRules(),
    val generators: Generators = Generators()
) : HttpPart() {

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
        generators.applyGenerator(Category.STATUS) { _, g -> r.status = g?.generate(r.status) as? Int ?: DEFAULT_STATUS }
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
    }
}