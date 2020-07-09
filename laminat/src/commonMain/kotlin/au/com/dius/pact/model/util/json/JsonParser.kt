package au.com.dius.pact.model.util.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

object JsonParser {

    private val internalJsonParser = Json(JsonConfiguration.Stable)

    fun parse(string: String?): JsonElement {
        if (string == null) {
            return JsonNull
        }
        return internalJsonParser.parseJson(string)
    }
}