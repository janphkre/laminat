package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.external.PactBuildException
import java.util.LinkedList

sealed class BodyMatchElement {

    fun asObject(): BodyMatchObject? {
        return this as? BodyMatchObject
    }

    fun asArray(): BodyMatchArray? {
        return this as? BodyMatchArray
    }

    fun asString(): BodyMatchString? {
        return this as? BodyMatchString
    }

    abstract fun equalsType(other: BodyMatchElement): Boolean
    abstract fun mergeFrom(other: BodyMatchElement)

    class BodyMatchObject(
        val entries: HashMap<String, BodyMatchElement> = HashMap()
    ) : BodyMatchElement() {

        fun entry(key: String): BodyMatchElement? {
            return entries[key]
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchObject
        }

        override fun mergeFrom(other: BodyMatchElement) {
            if (other is BodyMatchObject) {
                other.entries.forEach { keyValuePair ->
                    entries[keyValuePair.key]
                        ?.mergeFrom(keyValuePair.value)
                        ?: entries.put(keyValuePair.key, keyValuePair.value)
                }
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the object $this.")
            }
        }

        override fun toString(): String {
            return entries.entries.joinToString(separator = ",", prefix = "{", postfix = "}", truncated = "…", limit = MAX_OBJECT_STRING_LENGTH) {
                "${it.key}:${it.value}"
            }
        }
    }

    class BodyMatchArray(
        val element: BodyMatchElement
    ) : BodyMatchElement() {

        //We may decide to change out the array matching to index based elements later.
        @Suppress("UNUSED_PARAMETER")
        fun at(index: Int): BodyMatchElement? {
            return element
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchArray
        }

        override fun mergeFrom(other: BodyMatchElement) {
            if (other is BodyMatchArray) {
                element.mergeFrom(other.element)
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the array $this.")
            }
        }

        override fun toString(): String {
            return "[$element]"
        }
    }

    class BodyMatchString(
        val regex: String
    ) : BodyMatchElement() {

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchString
        }

        override fun mergeFrom(other: BodyMatchElement) {
            if (other is BodyMatchString) {
                throw PactBuildException("Merging of regexes is unsupported. Tried to merge \"$regex\" and \"${other.regex}\"")
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the regex \"$regex\".")
            }
        }

        override fun toString(): String {
            if (regex.length < MAX_ITEM_STRING_LENGTH) {
                return regex
            }
            return "${regex.take(MAX_ITEM_STRING_LENGTH - 1)}…"
        }
    }

    companion object {

        private const val MAX_ITEM_STRING_LENGTH = 7
        private const val MAX_OBJECT_STRING_LENGTH = 21

        fun from(matchRegexes: Map<String, String>): BodyMatchElement? {
            val startingPoints = LinkedList<BodyMatchElement>()
            matchRegexes.forEach { matchRegexPair ->
                val pathElements = matchRegexPair.key.split('.')
                if (pathElements.isEmpty()) {
                    throw PactBuildException("The path on a @MatchBody may not be empty. It must be separated by '.' characters.")
                }
                if (pathElements.first() != "$") {
                    throw PactBuildException("The path on a @MatchBody must start with the root specified by the '$' character. It must be separated by '.' characters.")
                }
                val resultMatch = pathElements.reversed().fold(BodyMatchString(matchRegexPair.value) as BodyMatchElement) { lastMatch, pathElement ->
                    when (pathElement) {
                        "[]" -> BodyMatchArray(lastMatch)
                        "$" -> lastMatch
                        else -> BodyMatchObject(hashMapOf(Pair(pathElement, lastMatch)))
                    }
                }
                startingPoints.firstOrNull { it.equalsType(resultMatch) }?.mergeFrom(resultMatch) ?: startingPoints.add(resultMatch)
            }
            if (startingPoints.size > 1) {
                throw PactBuildException("There was more than one root defined by @MatchBody annotations:\n${startingPoints.joinToString(separator = "\n")}")
            }
            return startingPoints.firstOrNull()
        }
    }
}