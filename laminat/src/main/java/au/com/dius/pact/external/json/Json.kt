package au.com.dius.pact.external.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.stream.JsonWriter
import java.io.Reader
import java.io.StringWriter

sealed interface Json {

    fun serialize(writer: JsonWriter)

    sealed interface Primitive : Json {

        fun isBoolean(): Boolean

        fun isString(): Boolean

        fun isNumber(): Boolean

        fun asBoolean(): Boolean

        fun asString(): String

        fun asNumber(): Number

        fun getValue(): Any

        data class BooleanPrimitive(
            private val value: Boolean
        ) : Primitive {
            override fun isBoolean(): Boolean = true

            override fun isString(): Boolean = false

            override fun isNumber(): Boolean = false

            override fun asBoolean(): Boolean = value

            override fun asString(): String {
                throw UnsupportedOperationException("Primitive is not a string!")
            }

            override fun asNumber(): Number {
                throw UnsupportedOperationException("Primitive is not a number!")
            }

            override fun getValue(): Any = value

            override fun serialize(writer: JsonWriter) {
                writer.value(value)
            }
        }

        data class StringPrimitive(
            private val value: String
        ) : Primitive {
            override fun isBoolean(): Boolean = false

            override fun isString(): Boolean = true

            override fun isNumber(): Boolean = false

            override fun asBoolean(): Boolean {
                throw UnsupportedOperationException("Primitive is not a boolean!")
            }

            override fun asString(): String = value

            override fun asNumber(): Number {
                throw UnsupportedOperationException("Primitive is not a number!")
            }

            override fun getValue(): Any = value

            override fun serialize(writer: JsonWriter) {
                writer.value(value)
            }
        }

        data class NumberPrimitive(
            private val value: Number
        ) : Primitive {
            override fun isBoolean(): Boolean = false

            override fun isString(): Boolean = false

            override fun isNumber(): Boolean = true

            override fun asBoolean(): Boolean {
                throw UnsupportedOperationException("Primitive is not a boolean!")
            }

            override fun asString(): String {
                throw UnsupportedOperationException("Primitive is not a string!")
            }

            override fun asNumber(): Number = value

            override fun getValue(): Any = value

            override fun serialize(writer: JsonWriter) {
                writer.value(value)
            }
        }
    }

    data class Object(
        private val elements: MutableMap<String, Json>
    ) : Json, MutableMap<String, Json> by elements {

        override fun serialize(writer: JsonWriter) {
            writer.beginObject()
            elements.forEach { (key, value) ->
                writer.name(key)
                value.serialize(writer)
            }
            writer.endObject()
        }
    }

    data class Array(
        private val elements: MutableList<Json>
    ) : Json, MutableList<Json> by elements {

        override fun serialize(writer: JsonWriter) {
            writer.beginArray()
            elements.forEach { value ->
                value.serialize(writer)
            }
            writer.endArray()
        }
    }

    object Null : Json {

        override fun serialize(writer: JsonWriter) {
            writer.nullValue()
        }
    }

    companion object {

        private fun convertToJson(gson: JsonElement): Json {
            return when (gson) {
                is JsonNull -> Null
                is JsonArray -> Array(gson.mapTo(ArrayList(gson.size())) { convertToJson(it) })
                is JsonObject -> {
                    val gsonEntries = gson.entrySet()
                    val entries = HashMap<String, Json>(gsonEntries.size)
                    gsonEntries.forEach {
                        entries[it.key] = convertToJson(it.value)
                    }
                    Object(entries)
                }
                is JsonPrimitive -> {
                    when {
                        gson.isBoolean -> Primitive.BooleanPrimitive(gson.asBoolean)
                        gson.isNumber -> Primitive.NumberPrimitive(gson.asNumber)
                        gson.isString -> Primitive.StringPrimitive(gson.asString)
                        else -> throw IllegalArgumentException("Received unknown Gson primitive: $gson")
                    }
                }
                else -> {
                    throw IllegalArgumentException("Received unknown Gson Element: ${gson::class.simpleName}")
                }
            }
        }

        fun parse(string: String): Json {
            val gsonElement = JsonParser.parseString(string)
            return convertToJson(gsonElement)
        }

        fun parse(reader: Reader?): Json {
            val gsonElement = JsonParser.parseReader(reader)
            return convertToJson(gsonElement)
        }

        fun serialize(json: Json): String {
            val stringWriter = StringWriter()
            val gsonWriter = JsonWriter(stringWriter)
            json.serialize(gsonWriter)
            return stringWriter.toString()
        }

        fun convertToJson(element: Any?): Json {
            if (element === null) {
                return Null
            }
            return when (element) {
                is Json -> element
                is Boolean -> Primitive.BooleanPrimitive(element)
                is Number -> Primitive.NumberPrimitive(element)
                is String -> Primitive.StringPrimitive(element)
                else -> throw java.lang.IllegalArgumentException("Can not convert $element to json!")
            }
        }
    }
}