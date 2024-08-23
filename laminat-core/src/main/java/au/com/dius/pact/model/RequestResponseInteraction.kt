package au.com.dius.pact.model

import au.com.dius.pact.external.util.toUtf8String
import au.com.dius.pact.model.serialization.SerializationConstants
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.min
import org.apache.http.Consts

data class RequestResponseInteraction(
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

    @Deprecated(
        "Use getProviderStates()",
        ReplaceWith("providerStates.firstOrNull()?.name ?: \"\"")
    )
    override val providerState: String
        get() {
            return providerStates.firstOrNull()?.name ?: ""
        }

    override fun conflictsWith(other: Interaction): Boolean {
        if (providerStates.containsAll(other.providerStates) && other.providerStates.containsAll(providerStates)) {
            if (other !is RequestResponseInteraction) {
                return false
            }
            return description == other.description || request == other.request
        }
        return false
    }

    override fun conflictsExactlyWith(other: Interaction): Boolean {
        if (providerStates.containsAll(other.providerStates) && other.providerStates.containsAll(providerStates)) {
            if (other !is RequestResponseInteraction) {
                return false
            }
            return description == other.description && request == other.request && response == other.response
        }
        return false
    }

    override fun toMap(serializationConfig: PactSerializationConfig): Map<*, *> {
        val interactionJson = mutableMapOf<String, Any?>(
            Pair(SerializationConstants.DESCRIPTION_KEY, description),
            Pair(SerializationConstants.REQUEST_KEY, requestToMap(request, serializationConfig)),
            Pair(SerializationConstants.RESPONSE_KEY, responseToMap(response, serializationConfig))
        )
        if (providerStates.isNotEmpty()) {
            if (serializationConfig.specVersion < PactSpecVersion.V3) {
                interactionJson[SerializationConstants.PROVIDER_STATE_KEY] = providerState
            } else {
                interactionJson[SerializationConstants.PROVIDER_STATES_KEY] = providerStates.map { it.toMap() }
            }
        }
        return interactionJson
    }

    override fun uniqueKey(): String {
        return "${displayState()}_$description"
    }

    companion object {
        fun requestToMap(request: Request, serializationConfig: PactSerializationConfig): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair(SerializationConstants.METHOD_KEY, request.method.uppercase(Locale.ROOT)),
                Pair(SerializationConstants.PATH_KEY, request.path)
            )
            if (request.headers.isNotEmpty()) {
                map[SerializationConstants.HEADERS_KEY] = request.headers
            }
            if (request.query.isNotEmpty()) {
                map[SerializationConstants.QUERY_KEY] = if (serializationConfig.specVersion >= PactSpecVersion.V3) request.query else mapToQueryStr(request.query)
            }
            if (request.body !is OptionalBody.MissingBody) {
                map[SerializationConstants.BODY_KEY] = parseBody(request, serializationConfig)
            }
            if (request.matchingRules.isNotEmpty()) {
                map[SerializationConstants.MATCHING_RULES_KEY] = request.matchingRules.toMap(serializationConfig)
            }
            if (request.generators.isNotEmpty() && serializationConfig.specVersion >= PactSpecVersion.V3) {
                map[SerializationConstants.GENERATORS_KEY] = request.generators.toMap(serializationConfig)
            }
            return map
        }

        fun responseToMap(response: Response, serializationConfig: PactSerializationConfig): Map<*, *> {
            val map = mutableMapOf<String, Any?>(
                Pair(SerializationConstants.STATUS_KEY, response.status)
            )
            if (response.headers.isNotEmpty()) {
                map[SerializationConstants.HEADERS_KEY] = response.headers
            }
            if (response.body !is OptionalBody.MissingBody) {
                map[SerializationConstants.BODY_KEY] = parseBody(response, serializationConfig)
            }
            if (response.matchingRules.isNotEmpty()) {
                map[SerializationConstants.MATCHING_RULES_KEY] = response.matchingRules.toMap(serializationConfig)
            }
            if (response.generators.isNotEmpty() && serializationConfig.specVersion >= PactSpecVersion.V3) {
                map[SerializationConstants.GENERATORS_KEY] = response.generators.toMap(serializationConfig)
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