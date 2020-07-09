package au.com.dius.pact.model.util.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement as KotlinxJsonElement

sealed class JsonElement {

    class Object(
        private val content: JsonObject
    ) : JsonElement(), Map<String, JsonElement> {

        override fun get(key: String): JsonElement? {
            return mapLibraryItem(content[key])
        }

        override val entries: Set<Map.Entry<String, JsonElement>> = Entries()

        override val keys: Set<String>
            get() = content.keys

        override val size: Int
            get() = content.size

        override val values: Collection<JsonElement> by lazy {
            content.values.map { mapLibraryItem(it) }
        }

        override fun containsKey(key: String): Boolean {
            return content.contains(key)
        }

        override fun containsValue(value: JsonElement): Boolean {
            throw NotImplementedError("The JsonObject map wrapper does not support value lookup.")
        }

        override fun isEmpty(): Boolean {
            return content.isEmpty()
        }

        private class Entry(
            override val key: String,
            override val value: JsonElement
        ) : Map.Entry<String, JsonElement>

        private inner class Entries : AbstractSet<Map.Entry<String, JsonElement>>() {
            override val size: Int
                get() = content.size

            override fun iterator(): Iterator<Map.Entry<String, JsonElement>> {
                return EntriesIterator(content.entries.iterator())
            }

            override fun contains(element: Map.Entry<String, JsonElement>): Boolean {
                return content[element.key] == element.value
            }
        }

        private class EntriesIterator(
            private val contentIterator: Iterator<Map.Entry<String, KotlinxJsonElement>>
        ): Iterator<Map.Entry<String, JsonElement>> {


            override fun hasNext(): Boolean {
                return contentIterator.hasNext()
            }

            override fun next(): Map.Entry<String, JsonElement> {
                val libraryEntry = contentIterator.next()
                return Entry(libraryEntry.key, mapLibraryItem(libraryEntry.value))
            }

        }
    }

    class Array(
        private val content: JsonArray
    ) : JsonElement(), Iterable<JsonElement> {

        fun get(index: Int): JsonElement {
            return mapLibraryItem(content[index])
        }

        fun size() : Int {
            return content.size
        }

        override fun iterator(): Iterator<JsonElement> {
            return ElementsIterator(content.iterator())
        }

        private class ElementsIterator(
            private val contentIterator: Iterator<KotlinxJsonElement>
        ): Iterator<JsonElement> {

            override fun hasNext(): Boolean {
                return contentIterator.hasNext()
            }

            override fun next(): JsonElement {
                return mapLibraryItem(contentIterator.next())
            }

        }

    }

    class Primitive(
        private val value: JsonPrimitive
    ) : JsonElement() {

        fun isWhole(): Boolean {
            return value.intOrNull != null || value.longOrNull != null
        }

        fun isDecimal(): Boolean {
            return value.floatOrNull != null || value.doubleOrNull != null
        }

        fun isBoolean(): Boolean {
            return value.booleanOrNull != null
        }

        fun isString(): Boolean {
            return value.contentOrNull != null
        }

        fun getValue(): Any? {
            return value.contentOrNull ?: value.booleanOrNull ?: value.intOrNull ?: value.longOrNull ?: value.floatOrNull ?: value.doubleOrNull
        }
    }

    object Null : JsonElement()

    companion object {
        fun mapLibraryItem(item: KotlinxJsonElement?): JsonElement {
            return when(item) {
                is JsonObject -> {
                    Object(item)
                }
                is JsonArray -> {
                    Array(item)
                }
                is JsonPrimitive -> {
                    Primitive(item)
                }
                else -> Null
            }
        }
    }
}