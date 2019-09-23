package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.BasePact
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okio.Buffer

object DslJsonBodyConverter: DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer): DslPart {
        val jsonBody = retrofitBody.inputStream().use { BasePact.jsonParser.parse(it.reader()) }
        //TODO: WHAT TO DO WHEN THE BASE IS NOT A JSON OBJECT BUT RATHER A JSON PRIMITIVE?
        return jsonElementToDsl(jsonBody, "", null) ?: TODO()
    }

    private fun jsonElementToDsl(jsonElement: JsonElement, key: String, pactDslJsonBody: PactDslJsonBody?): DslPart? {
        return when {
            jsonElement.isJsonObject -> {
                val dslObject = pactDslJsonBody?.`object`(key) ?: PactDslJsonBody()
                jsonObjectToDsl(jsonElement.asJsonObject, dslObject)
                dslObject.closeObject() ?: dslObject
            }
            jsonElement.isJsonArray -> {
                val dslArray = pactDslJsonBody?.array(key) ?: PactDslJsonArray()
                jsonArrayToDsl(jsonElement.asJsonArray)
                dslArray.closeArray() ?: dslArray
            }
            jsonElement.isJsonPrimitive -> jsonPrimitiveToDsl(jsonElement.asJsonPrimitive, TODO())
            jsonElement.isJsonNull -> null
            else -> null
        }
    }

    private fun jsonObjectToDsl(jsonObject: JsonObject, dslObject: PactDslJsonBody): DslPart {
        jsonObject.entrySet().forEach {
            jsonElementToDsl(it.value, it.key, dslObject)
        }
        return dslObject
    }

    private fun jsonArrayToDsl(jsonArray: JsonArray): DslPart {
        TODO()
    }

    private fun jsonPrimitiveToDsl(jsonPrimitive: JsonPrimitive, key: String, dslObject: PactDslJsonBody): PactDslJsonBody {
        when {
            jsonPrimitive.isBoolean -> dslObject.booleanType(key, jsonPrimitive.asBoolean)
            jsonPrimitive.isNumber -> dslObject.numberType(key, jsonPrimitive.asNumber)
            jsonPrimitive.isString -> dslObject.stringType(key, jsonPrimitive.asString)
        }
        //TODO
    }
}