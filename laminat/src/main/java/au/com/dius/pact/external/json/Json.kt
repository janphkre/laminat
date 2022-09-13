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

    operator fun get(key: String): Json

    operator fun get(index: Int): Json

    fun <T> getValue(): T
    fun <T> getValueOrNull(): T?

    sealed interface Primitive : Json

    data class BooleanPrimitive(
        private val value: Boolean
    ) : Primitive {

        override fun get(index: Int): Json {
            throw UnsupportedOperationException("Primitive is not an array!")
        }

        override fun get(key: String): Json {
            throw UnsupportedOperationException("Primitive is not an object!")
        }

        fun asBoolean(): Boolean = value

        @Suppress("UNCHECKED_CAST")
        override fun <T> getValue(): T = value as T
        override fun <T> getValueOrNull(): T? = getValue()

        override fun serialize(writer: JsonWriter) {
            writer.value(value)
        }

        override fun toString(): String {
            return Companion.serialize(this)
        }
    }

    data class StringPrimitive(
        private val value: String
    ) : Primitive {

        override fun get(index: Int): Json {
            throw UnsupportedOperationException("Primitive is not an array!")
        }

        override fun get(key: String): Json {
            throw UnsupportedOperationException("Primitive is not an object!")
        }

        fun asString(): String = value

        @Suppress("UNCHECKED_CAST")
        override fun <T> getValue(): T = value as T
        override fun <T> getValueOrNull(): T? = getValue()

        override fun serialize(writer: JsonWriter) {
            writer.value(value)
        }

        override fun toString(): String {
            return Companion.serialize(this)
        }
    }

    data class NumberPrimitive(
        private val value: Number
    ) : Primitive {

        override fun get(index: Int): Json {
            throw UnsupportedOperationException("Primitive is not an array!")
        }

        override fun get(key: String): Json {
            throw UnsupportedOperationException("Primitive is not an object!")
        }

        fun asNumber(): Number = value

        @Suppress("UNCHECKED_CAST")
        override fun <T > getValue(): T = value as T
        override fun <T> getValueOrNull(): T? = getValue()

        override fun serialize(writer: JsonWriter) {
            writer.value(value)
        }

        override fun toString(): String {
            return Companion.serialize(this)
        }
    }

    data class Object(
        private val elements: MutableMap<String, Json>
    ) : Json, MutableMap<String, Json> by elements {

        constructor(vararg elementPairs: Pair<String,Json>): this(mutableMapOf(*elementPairs))

        override fun get(index: Int): Json {
            throw UnsupportedOperationException("Object is not an array!")
        }

        override fun get(key: String): Json {
            return elements[key] ?: Null
        }

        override fun <T> getValue(): T {
            throw UnsupportedOperationException("Object is not a primitive!")
        }

        override fun <T> getValueOrNull(): T? = getValue()

        override fun serialize(writer: JsonWriter) {
            writer.beginObject()
            elements.forEach { (key, value) ->
                writer.name(key)
                value.serialize(writer)
            }
            writer.endObject()
        }

        override fun toString(): String {
            return serialize(this)
        }
    }

    data class Array(
        private val elements: MutableList<Json>
    ) : Json, MutableList<Json> by elements {

        constructor(vararg elementList: Json): this(mutableListOf(*elementList))


        override fun get(index: Int): Json {
            return elements[index]
        }

        override fun get(key: String): Json {
            throw UnsupportedOperationException("Array is not an object!")
        }

        override fun <T > getValue(): T {
            throw UnsupportedOperationException("Array is not a primitive!")
        }

        override fun <T> getValueOrNull(): T? = getValue()

        override fun serialize(writer: JsonWriter) {
            writer.beginArray()
            elements.forEach { value ->
                value.serialize(writer)
            }
            writer.endArray()
        }

        override fun toString(): String {
            return serialize(this)
        }
    }

    object Null : Json {

        override fun get(index: Int): Json {
            throw UnsupportedOperationException("Null is not an array!")
        }

        override fun get(key: String): Json {
            throw UnsupportedOperationException("Null is not an object!")
        }

        override fun <T> getValue(): T {
            throw UnsupportedOperationException("Null is not a primitive!")
        }

        override fun <T> getValueOrNull(): T? = null

        override fun serialize(writer: JsonWriter) {
            writer.nullValue()
        }

        override fun toString(): String {
            return serialize(this)
        }
    }

    companion object {

        fun convertToJson(gson: JsonElement?): Json {
            return when (gson) {
                null -> Null
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
                        gson.isBoolean -> BooleanPrimitive(gson.asBoolean)
                        gson.isNumber -> NumberPrimitive(gson.asNumber)
                        gson.isString -> StringPrimitive(gson.asString)
                        else -> throw IllegalArgumentException("Received unknown Gson primitive: $gson")
                    }
                }
                else -> {
                    throw IllegalArgumentException("Received unknown Gson Element: ${gson::class.simpleName}")
                }
            }
        }

        fun convertToGson(json: Json?): JsonElement {
            return when(json) {
                null -> JsonNull.INSTANCE
                Null -> JsonNull.INSTANCE
                is Array -> JsonArray().apply {
                    json.forEach { entry ->
                        this.add(convertToGson(entry))
                    }
                }
                is Object -> JsonObject().apply {
                    json.forEach { (key, value) ->
                        this.add(key, convertToGson(value))
                    }
                }
                is BooleanPrimitive -> JsonPrimitive(json.asBoolean())
                is NumberPrimitive -> JsonPrimitive(json.asNumber())
                is StringPrimitive -> JsonPrimitive(json.asString())
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

        fun wrapInJson(element: Any?): Json {
            if (element === null) {
                return Null
            }
            return when (element) {
                is Json -> element
                is Boolean -> BooleanPrimitive(element)
                is Number -> NumberPrimitive(element)
                is String -> StringPrimitive(element)
                else -> throw java.lang.IllegalArgumentException("Can not convert $element to json!")
            }
        }
    }
}