package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.external.PactBuildException
import au.com.dius.pact.model.BasePact
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okio.Buffer

object DslJsonBodyConverter: DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer): DslPart {
        val jsonBody = retrofitBody.inputStream().use { BasePact.jsonParser.parse(it.reader()) }
        return jsonRootToDsl(jsonBody)
    }

    private fun jsonRootToDsl(jsonElement: JsonElement): DslPart {
        return when {
            jsonElement.isJsonObject -> jsonObjectToDsl("", jsonElement.asJsonObject, null)
            jsonElement.isJsonArray -> jsonArrayToDsl("", jsonElement.asJsonArray, null)
            jsonElement.isJsonPrimitive -> jsonPrimitiveToDslRoot(jsonElement.asJsonPrimitive)
            jsonElement.isJsonNull -> throw PactBuildException("A json null value as the root of a request is unsupported.")
            else -> raiseException(jsonElement)
        }
    }

    private fun jsonElementToDsl(keyInParent: String?, jsonElement: JsonElement, parent: DslPart): DslPart? {
        return when {
            jsonElement.isJsonObject -> jsonObjectToDsl(keyInParent, jsonElement.asJsonObject, parent)
            jsonElement.isJsonArray -> jsonArrayToDsl(keyInParent, jsonElement.asJsonArray, parent)
            jsonElement.isJsonPrimitive -> jsonPrimitiveToDsl(keyInParent, jsonElement.asJsonPrimitive, parent)
            jsonElement.isJsonNull -> null
            //TODO: DON'T RETURN NULL HERE SINCE THERE IS STILL A LOGICAL TYPE. Also, null values should be omitted in the pact imo.
            else -> raiseException(jsonElement)
        }
    }

    private fun jsonObjectToDsl(keyInParent: String?, jsonObject: JsonObject, parent: DslPart?): DslPart {
        val dslObject = if(keyInParent != null) {
            parent?.`object`(keyInParent)
        } else {
            parent?.`object`()
        } ?: PactDslJsonBody()
        jsonObject.entrySet().forEach {
            jsonElementToDsl(it.key, it.value, dslObject)
        }
        return dslObject.closeObject() ?: dslObject
    }

    private fun jsonArrayToDsl(keyInParent: String?, jsonArray: JsonArray, parent: DslPart?): DslPart {
        val dslArray = if(keyInParent != null) {
            parent?.array(keyInParent)
        } else {
            parent?.array()
        } ?: PactDslJsonArray()
        jsonArray.forEach {
            jsonElementToDsl(null, it, dslArray)
        }
        return dslArray.closeArray() ?: dslArray
    }

    private fun jsonPrimitiveToDsl(keyInParent: String?, jsonPrimitive: JsonPrimitive, parent: DslPart): DslPart {
        return when(parent) {
            is PactDslJsonBody -> jsonPrimitiveToDslObject(keyInParent ?: raiseException(jsonPrimitive), jsonPrimitive, parent)
            is PactDslJsonArray -> jsonPrimitiveToDslArray(jsonPrimitive, parent)
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonPrimitiveToDslRoot(jsonPrimitive: JsonPrimitive): DslPart {
        return when {
            jsonPrimitive.isBoolean -> PactDslJsonRootValue.booleanType(jsonPrimitive.asBoolean)
            jsonPrimitive.isNumber -> PactDslJsonRootValue.numberType(jsonPrimitive.asNumber)
            jsonPrimitive.isString -> PactDslJsonRootValue.stringType(jsonPrimitive.asString)
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonPrimitiveToDslArray(jsonPrimitive: JsonPrimitive, parent: PactDslJsonArray): DslPart {
        when {
            jsonPrimitive.isBoolean -> parent.booleanType(jsonPrimitive.asBoolean)
            jsonPrimitive.isNumber -> parent.numberType(jsonPrimitive.asNumber)
            jsonPrimitive.isString -> parent.stringType(jsonPrimitive.asString)
            else -> raiseException(jsonPrimitive)
        }
        return parent
    }

    private fun jsonPrimitiveToDslObject(keyInParent: String, jsonPrimitive: JsonPrimitive, parent: PactDslJsonBody): DslPart {
        when {
            jsonPrimitive.isBoolean -> parent.booleanType(keyInParent, jsonPrimitive.asBoolean)
            jsonPrimitive.isNumber -> parent.numberType(keyInParent, jsonPrimitive.asNumber)
            jsonPrimitive.isString -> parent.stringType(keyInParent, jsonPrimitive.asString)
            else -> raiseException(jsonPrimitive)
        }
        return parent
    }

    private fun raiseException(jsonElement: JsonElement) : Nothing {
        throw PactBuildException("Unsupported json found in $jsonElement")
    }
}