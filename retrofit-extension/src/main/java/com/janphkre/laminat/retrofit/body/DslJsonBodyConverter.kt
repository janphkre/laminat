package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.external.PactBuildException
import au.com.dius.pact.external.json.Json
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import okio.Buffer

object DslJsonBodyConverter : DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer, bodyMatches: BodyMatchElement?): DslPart {
        val jsonBody = retrofitBody.inputStream().use { Json.parse(it.reader()) }
        return jsonRootToDsl(jsonBody, bodyMatches)
    }

    private fun jsonRootToDsl(jsonElement: Json, bodyMatches: BodyMatchElement?): DslPart {
        return when (jsonElement) {
            is Json.Object -> {
                jsonObjectToDsl("", jsonElement, null, bodyMatches?.asObject())
            }
            is Json.Array -> {
                jsonArrayToDsl("", jsonElement, null, bodyMatches?.asArray())
            }
            is Json.Primitive -> {
                jsonPrimitiveToDslRoot(jsonElement, bodyMatches?.asString())
            }
            Json.Null -> {
                PactDslJsonRootValue.matchNull()
            }
        }
    }

    private fun jsonElementToDsl(
        keyInParent: String?,
        jsonElement: Json,
        parent: DslPart,
        bodyMatches: BodyMatchElement?
    ): DslPart {
        return when (jsonElement) {
            is Json.Object -> {
                jsonObjectToDsl(
                    keyInParent,
                    jsonElement,
                    parent,
                    bodyMatches?.asObject()
                )
            }
            is Json.Array -> {
                jsonArrayToDsl(
                    keyInParent,
                    jsonElement,
                    parent,
                    bodyMatches?.asArray()
                )
            }
            is Json.Primitive -> {
                jsonPrimitiveToDsl(
                    keyInParent,
                    jsonElement,
                    parent,
                    bodyMatches?.asString()
                )
            }
            Json.Null -> {
                jsonNullToDsl(keyInParent, parent)
            }
        }
    }

    private fun jsonObjectToDsl(
        keyInParent: String?,
        jsonObject: Json.Object,
        parent: DslPart?,
        bodyMatches: BodyMatchElement.BodyMatchObject?
    ): DslPart {
        // TODO USE EACH KEY LIKE?
        val dslObject = if (keyInParent != null) {
            parent?.`object`(keyInParent)
        } else {
            parent?.`object`()
        } ?: PactDslJsonBody()
        jsonObjectToDirectDsl(jsonObject, dslObject, bodyMatches)
        return dslObject.closeObject() ?: dslObject
    }

    private fun jsonArrayToDsl(
        keyInParent: String?,
        jsonArray: Json.Array,
        parent: DslPart?,
        bodyMatches: BodyMatchElement.BodyMatchArray?
    ): DslPart {
        // TODO: THIS IS ONLY WORKING FOR ARRAYS OF OBJECTS ATM
        val dslArray = when (bodyMatches) {
            is BodyMatchElement.BodyMatchMinArray -> {
                val dslArrayElement = if (keyInParent != null) {
                    parent?.minArrayLike(keyInParent, bodyMatches.minCount)
                } else {
                    parent?.minArrayLike(bodyMatches.minCount)
                } ?: PactDslJsonArray("", "", null, true).let {
                    it.matchers.addRule("", MinTypeMatcher(bodyMatches.minCount))
                    it.numberExamples = bodyMatches.minCount
                    PactDslJsonBody(".", "", it)
                }

                jsonArrayToDirectDsl(jsonArray.first(), dslArrayElement, bodyMatches.at(0))
            }
            is BodyMatchElement.BodyMatchMaxArray -> {
                val dslArrayElement = if (keyInParent != null) {
                    parent?.maxArrayLike(keyInParent, bodyMatches.maxCount)
                } else {
                    parent?.maxArrayLike(bodyMatches.maxCount)
                } ?: PactDslJsonArray("", "", null, true).let {
                    it.matchers.addRule("", MaxTypeMatcher(bodyMatches.maxCount))
                    it.numberExamples = bodyMatches.maxCount
                    PactDslJsonBody(".", "", it)
                }

                jsonArrayToDirectDsl(jsonArray.first(), dslArrayElement, bodyMatches.at(0))
            }
            else -> {
                val dslArray = if (keyInParent != null) {
                    parent?.array(keyInParent)
                } else {
                    parent?.array()
                } ?: PactDslJsonArray()

                jsonArray.forEachIndexed { index, jsonElement ->
                    jsonElementToDsl(null, jsonElement, dslArray, bodyMatches?.at(index))
                }
                dslArray
            }
        }
        return dslArray.closeArray() ?: dslArray
    }

    private fun jsonArrayToDirectDsl(
        jsonArrayElement: Json,
        dslArrayElement: PactDslJsonBody,
        arrayElementMatches: BodyMatchElement?
    ): DslPart {
        if (jsonArrayElement !is Json.Object) {
            throw PactBuildException(
                "arrayLike of ${jsonArrayElement.javaClass.name} is not supported by pact dsl!"
            )
        }
        if (arrayElementMatches !is BodyMatchElement.BodyMatchObject?) {
            throw PactBuildException(
                "arrayLike of ${arrayElementMatches?.javaClass?.name} is not supported by pact dsl!"
            )
        }
        jsonObjectToDirectDsl(jsonArrayElement, dslArrayElement, arrayElementMatches)
        return dslArrayElement.closeObject()
            ?: throw PactBuildException("Closing the inner object of an JsonArray returned null!")
    }

    private fun jsonObjectToDirectDsl(
        jsonObject: Json.Object,
        dslObject: PactDslJsonBody,
        bodyMatches: BodyMatchElement.BodyMatchObject?
    ) {
        jsonObject.forEach {
            jsonElementToDsl(it.key, it.value, dslObject, bodyMatches?.entry(it.key))
        }
    }

    private fun jsonPrimitiveToDsl(
        keyInParent: String?,
        jsonPrimitive: Json.Primitive,
        parent: DslPart,
        bodyMatches: BodyMatchElement.BodyMatchString?
    ): DslPart {
        return when (parent) {
            is PactDslJsonBody -> jsonPrimitiveToDslObject(
                keyInParent ?: raiseException(jsonPrimitive),
                jsonPrimitive,
                parent,
                bodyMatches
            )
            is PactDslJsonArray -> jsonPrimitiveToDslArray(jsonPrimitive, parent, bodyMatches)
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonPrimitiveToDslArray(
        jsonPrimitive: Json.Primitive,
        parent: PactDslJsonArray,
        bodyMatches: BodyMatchElement.BodyMatchString?
    ): DslPart {
        return when {
            jsonPrimitive is Json.BooleanPrimitive && bodyMatches == null -> {
                parent.booleanType(jsonPrimitive.asBoolean())
            }
            jsonPrimitive is Json.NumberPrimitive && bodyMatches == null -> {
                parent.numberType(jsonPrimitive.asNumber())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches == null -> {
                parent.stringType(jsonPrimitive.asString())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches != null -> {
                parent.stringMatcher(bodyMatches.regex, jsonPrimitive.asString())
            }
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonPrimitiveToDslObject(
        keyInParent: String,
        jsonPrimitive: Json.Primitive,
        parent: PactDslJsonBody,
        bodyMatches: BodyMatchElement.BodyMatchString?
    ): DslPart {
        return when {
            jsonPrimitive is Json.BooleanPrimitive && bodyMatches == null -> {
                parent.booleanType(keyInParent, jsonPrimitive.asBoolean())
            }
            jsonPrimitive is Json.NumberPrimitive && bodyMatches == null -> {
                parent.numberType(keyInParent, jsonPrimitive.asNumber())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches == null -> {
                parent.stringType(keyInParent, jsonPrimitive.asString())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches != null -> {
                parent.stringMatcher(keyInParent, bodyMatches.regex, jsonPrimitive.asString())
            }
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonPrimitiveToDslRoot(
        jsonPrimitive: Json.Primitive,
        bodyMatches: BodyMatchElement.BodyMatchString?
    ): DslPart {
        return when {
            jsonPrimitive is Json.BooleanPrimitive && bodyMatches == null -> {
                PactDslJsonRootValue.booleanType(jsonPrimitive.asBoolean())
            }
            jsonPrimitive is Json.NumberPrimitive && bodyMatches == null -> {
                PactDslJsonRootValue.numberType(jsonPrimitive.asNumber())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches == null -> {
                PactDslJsonRootValue.stringType(jsonPrimitive.asString())
            }
            jsonPrimitive is Json.StringPrimitive && bodyMatches != null -> {
                PactDslJsonRootValue.stringMatcher(bodyMatches.regex, jsonPrimitive.asString())
            }
            else -> raiseException(jsonPrimitive)
        }
    }

    private fun jsonNullToDsl(keyInParent: String?, parent: DslPart): DslPart {
        return when (parent) {
            is PactDslJsonBody -> parent.nullValue(keyInParent ?: raiseException(Json.Null))
            is PactDslJsonArray -> parent.nullValue()
            else -> raiseException(Json.Null)
        }
    }

    private fun raiseException(jsonElement: Json): Nothing {
        throw PactBuildException("Unsupported json found in $jsonElement")
    }
}