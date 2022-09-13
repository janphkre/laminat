package au.com.dius.pact.external.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class JsonGsonAdapter : JsonSerializer<Json>, JsonDeserializer<Json> {

    override fun serialize(src: Json?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return Json.convertToGson(src)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Json {
        return Json.convertToJson(json)
    }
}