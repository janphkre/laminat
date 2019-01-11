package au.com.dius.pact.external

import au.com.dius.pact.model.*
import au.com.dius.pact.model.BasePact.Companion.jsonParser
import au.com.dius.pact.model.matchingrules.MatchingRules

abstract class JsonRequestPactMatcher<R, S> {

    fun findInteraction(
        pacts: List<RequestResponsePact>,
        states: List<ProviderState>,
        method: String,
        path: String,
        query: R,
        headers: S,
        body: String
    ): RequestResponseInteraction? {
        pacts.forEach { pact ->
            return pact.requestResponseInteractions.firstOrNull { interaction ->
                states.containsAll(interaction.providerStates) &&
                interaction.request.matches(method, path, query, headers, body)
            }
        }
        return null
    }

    private fun Request.matches(method: String, path: String, query: R, headers: S, body: String): Boolean {
        return this.method == method &&
                this.path == path &&
                this.query.matchQuery(query) &&
                this.headersWithoutCookie().matchHeader(headers) &&
                this.body.matchBody(body) &&
                this.matchingRules.matchRules(method, path, query, headers, body)
    }

    abstract fun Map<String, List<String>>.matchQuery(userQuery: R): Boolean

    abstract fun Map<String, String>?.matchHeader(userHeaders: S): Boolean

    abstract fun MatchingRules.matchRules(method: String, path: String, query: R, headers: S, body: String): Boolean

    private fun OptionalBody?.matchBody(userBody: String): Boolean {
        if(this?.isPresent() != true && userBody.isEmpty()) {
            return true
        }
        val pactJson = jsonParser.parse(this!!.unwrap())
        val userJson = jsonParser.parse(userBody)

        return pactJson == userJson
    }
}