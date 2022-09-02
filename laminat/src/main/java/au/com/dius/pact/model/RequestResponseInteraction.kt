package au.com.dius.pact.model

import au.com.dius.pact.matchers.toUtf8String
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.min
import org.apache.http.Consts

class RequestResponseInteraction(
    override val description: String,
    override val providerStates: List<ProviderState>,
    val request: Request,
    val response: Response
) : Interaction {

    override fun toString(): String {
        return "Interaction: $description\n\tin states ${displayState()}\nrequest:\n$request\n"
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
            return if (providerStates.isEmpty()) "" else providerStates.first().name
        }

    override fun conflictsWith(other: Interaction): Boolean {
        if (providerStates.containsAll(other.providerStates) && other.providerStates.containsAll(providerStates)) {
            if (other !is RequestResponseInteraction) {
                return false
            }
            return request == other.request
        }
        return false
    }

    override fun toMap(serializationConfig: PactSerializationConfig): Map<*, *> {
        val interactionJson = mutableMapOf<String, Any?>(
            Pair("description", description),
            Pair("request", requestToMap(request, serializationConfig)),
            Pair("response", responseToMap(response, serializationConfig))
        )
        if (serializationConfig.specVersion < PactSpecVersion.V3 && !providerStates.isEmpty()) {
            interactionJson["providerState"] = providerState
        } else if (!providerStates.isEmpty()) {
            interactionJson["providerStates"] = providerStates.map { it.toMap() }
        }
        return interactionJson
    }

    override fun uniqueKey(): String {
        return "${displayState()}_$description"
    }

    companion object {
        fun requestToMap(request: Request, serializationConfig: PactSerializationConfig): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair("method", request.method.uppercase(Locale.ROOT)),
                Pair("path", request.path)
            )
            if (request.headers.isNotEmpty()) {
                map["headers"] = request.headers
            }
            if (request.query.isNotEmpty()) {
                map["query"] = if (serializationConfig.specVersion >= PactSpecVersion.V3) request.query else mapToQueryStr(request.query)
            }
            if (request.body !is OptionalBody.MissingBody) {
                map["body"] = parseBody(request, serializationConfig)
            }
            if (request.matchingRules.isNotEmpty()) {
                map["matchingRules"] = request.matchingRules.toMap(serializationConfig)
            }
            if (request.generators.isNotEmpty() && serializationConfig.specVersion >= PactSpecVersion.V3) {
                map["generators"] = request.generators.toMap(serializationConfig)
            }
            return map
        }

        fun responseToMap(response: Response, serializationConfig: PactSerializationConfig): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair("status", response.status)
            )
            if (response.headers.isNotEmpty()) {
                map["headers"] = response.headers
            }
            if (response.body !is OptionalBody.MissingBody) {
                map["body"] = parseBody(response, serializationConfig)
            }
            if (response.matchingRules.isNotEmpty()) {
                map["matchingRules"] = response.matchingRules.toMap(serializationConfig)
            }
            if (response.generators.isNotEmpty() && serializationConfig.specVersion >= PactSpecVersion.V3) {
                map["generators"] = response.generators.toMap(serializationConfig)
            }
            return map
        }

        fun mapToQueryStr(query: Map<String, List<String>>): String {
            return query.flatMap { entry -> entry.value.map { "${entry.key}=${URLEncoder.encode(it, Consts.UTF_8.name())}" } }.joinToString("&")
        }

        fun parseBody(httpPart: HttpPart, serializationConfig: PactSerializationConfig): Any? {
            return when (val body = httpPart.body) {
                is OptionalBody.StringBody -> {
                    if (httpPart.jsonBody()) {
                        body.unwrapJson()
                    } else {
                        body.unwrap()
                    }
                }
                is OptionalBody.BinaryBody -> {
                    var unwrappedBody = body.unwrap()
                    val truncateBinaryLength = min(unwrappedBody.size, serializationConfig.truncateBinaryLength ?: Int.MAX_VALUE)
                    if (truncateBinaryLength >= 0) {
                        unwrappedBody = unwrappedBody.sliceArray(0 until truncateBinaryLength)
                    }
                    unwrappedBody.toUtf8String()
                }
                else -> {
                    null
                }
            }
        }
    }
}