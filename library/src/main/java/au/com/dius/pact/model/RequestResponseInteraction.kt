package au.com.dius.pact.model

import com.google.gson.JsonParser
import java.net.URLEncoder

class RequestResponseInteraction(
    override val description: String,
    override val providerStates: List<ProviderState>,
    val request: Request,
    val response: Response
) : Interaction {

    override fun toString(): String {
        return "Interaction: $description\n\tin states ${displayState()}\nrequest:\n$request\n\nresponse:\n$response"
    }

    fun displayState(): String {
        return if (providerStates.isEmpty()) {
            "None"
        } else {
            providerStates.joinToString(separator = ", ") { it.name }
        }
    }

    override val providerState: String
        get() {
            return if(providerStates.isEmpty()) "" else  providerStates.first().name
        }

    override fun conflictsWith(other: Interaction): Boolean {
        if(providerStates.containsAll(other.providerStates) && other.providerStates.containsAll(providerStates)) {
            if(other !is RequestResponseInteraction) {
                return false
            }
            return request == other.request
        }
        return false

    }

    override fun toMap(pactSpecVersion: PactSpecVersion): Map<*, *> {
        val interactionJson = mutableMapOf<String, Any?>(
            Pair("description", description),
            Pair("request", requestToMap(request, pactSpecVersion)),
            Pair("response", responseToMap(response, pactSpecVersion))
        )
        if (pactSpecVersion < PactSpecVersion.V3 && !providerStates.isEmpty()) {
            interactionJson["providerState"] = providerState
        } else if (!providerStates.isEmpty()) {
            interactionJson["providerStates"] = providerStates.map { it.toMap() }
        }
        return interactionJson
    }

    override fun toMap(): Map<*, *> {
        return toMap(PactSpecVersion.V3)
    }

    override fun uniqueKey(): String {
        return "${displayState()}_$description"
    }

    companion object {
        fun requestToMap(request: Request, pactSpecVersion: PactSpecVersion): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair("method", request.method.toUpperCase()),
                Pair("path", request.path)
            )
            if (request.headers.isNotEmpty()) {
                map.set("headers", request.headers)
            }
            if (request.query.isNotEmpty()) {
                map.set("query", if(pactSpecVersion >= PactSpecVersion.V3) request.query else mapToQueryStr(request.query))
            }
            if (!request.body.isMissing()) {
                map.set("body", parseBody(request))
            }
            if (request.matchingRules.isNotEmpty()) {
                map.set("matchingRules", request.matchingRules.toMap(pactSpecVersion))
            }
            if (request.generators.isNotEmpty() && pactSpecVersion >= PactSpecVersion.V3) {
                map.set("generators", request.generators.toMap(pactSpecVersion))
            }
            return map
        }

        fun responseToMap(response: Response, pactSpecVersion: PactSpecVersion): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair("status", response.status)
            )
            if (response.headers.isNotEmpty()) {
                map.set("headers", response.headers)
            }
            if (!response.body.isMissing()) {
                map.set("body", parseBody(response))
            }
            if (response.matchingRules.isNotEmpty()) {
                map.set("matchingRules", response.matchingRules.toMap(pactSpecVersion))
            }
            if (response.generators.isNotEmpty() && pactSpecVersion >= PactSpecVersion.V3) {
                map.set("generators", response.generators.toMap(pactSpecVersion))
            }
            return map
        }

        fun mapToQueryStr(query: Map<String, List<String>>): String {
            return query.flatMap { entry -> entry.value.map { "${entry.key}=${URLEncoder.encode(it, "UTF-8")}" } }.joinToString("&")
        }

        fun parseBody(httpPart: HttpPart): Any? {
            return if (httpPart.jsonBody() && httpPart.body.isPresent()) {
                JsonParser().parse(httpPart.body.value)
            } else {
                httpPart.body.value
            }
        }
    }
}