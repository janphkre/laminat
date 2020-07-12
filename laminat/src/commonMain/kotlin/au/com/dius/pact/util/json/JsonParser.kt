package au.com.dius.pact.util.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object JsonParser {

    private val internalJsonParser = Json(JsonConfiguration.Stable)

    fun parse(string: String?): JsonElement {
        if (string == null) {
            return JsonElement.Null
        }
        return JsonElement.mapLibraryItem(internalJsonParser.parseJson(string))
    }
}