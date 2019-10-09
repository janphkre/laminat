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
    abstract fun mergeFrom(other: BodyMatchElement?): BodyMatchElement

    class BodyMatchObject(
        internal val entries: HashMap<String, BodyMatchElement> = HashMap()
    ) : BodyMatchElement() {

        fun entry(key: String): BodyMatchElement? {
            return entries[key]
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchObject
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            if (other is BodyMatchObject) {
                other.entries.forEach { keyValuePair ->
                    entries[keyValuePair.key]
                        ?.mergeFrom(keyValuePair.value)
                        ?: entries.put(keyValuePair.key, keyValuePair.value)
                }
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the object $this.")
            }
            return this
        }

        override fun toString(): String {
            return entries.entries.joinToString(separator = ",", prefix = "{", postfix = "}", truncated = "…", limit = MAX_OBJECT_STRING_LENGTH) {
                "${it.key}:${it.value}"
            }
        }
    }

    abstract class BodyMatchArray(
        internal var element: BodyMatchElement?
    ) : BodyMatchElement() {

        //We may decide to change out the array matching to index based elements later.
        @Suppress("UNUSED_PARAMETER")
        open fun at(index: Int): BodyMatchElement? {
            return element
        }

        override fun toString(): String {
            return "[$element]"
        }
    }

    class BodyMatchAbsoluteArray(element: BodyMatchElement?) : BodyMatchArray(element) {

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchAbsoluteArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            if (other is BodyMatchAbsoluteArray) {
                element = element?.mergeFrom(other.element) ?: other.element
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the array $this.")
            }
            return this
        }
    }

    class BodyMatchMinArray(
        element: BodyMatchElement?,
        internal val minCount: Int
    ) : BodyMatchArray(element) {

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchMinArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            element = when (other) {
                is BodyMatchMinArray -> {
                    if (minCount != other.minCount) {
                        throw PactBuildException("Found conflicting minCount $minCount & ${other.minCount} while merging $other into the min array $this.")
                    }
                    element?.mergeFrom(other.element) ?: other.element
                }
                is BodyMatchAbsoluteArray -> element?.mergeFrom(other.element) ?: other.element
                else -> throw PactBuildException("Found conflicting types while merging $other into the min array $this.")
            }
            return this
        }
    }

    class BodyMatchMaxArray(
        element: BodyMatchElement?,
        internal val maxCount: Int
    ) : BodyMatchArray(element) {

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchMaxArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            element = when (other) {
                is BodyMatchMaxArray -> {
                    if (maxCount != other.maxCount) {
                        throw PactBuildException("Found conflicting maxCount $maxCount & ${other.maxCount} while merging $other into the max array $this.")
                    }
                    element?.mergeFrom(other.element) ?: other.element
                }
                is BodyMatchAbsoluteArray -> element?.mergeFrom(other.element) ?: other.element
                else -> throw PactBuildException("Found conflicting types while merging $other into the max array $this.")
            }
            return this
        }
    }

    class BodyMatchString(
        internal val regex: String
    ) : BodyMatchElement() {

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchString
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchString {
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

        fun from(matchRegexes: Map<String, String>, matchArrays: Map<String, Int>): BodyMatchElement? {
            val startingPoints = LinkedList<BodyMatchElement>()
            matchArrays.forEach { matchArrayPair ->
                val startElement = if (matchArrayPair.value > 0) {
                    BodyMatchMaxArray(null, matchArrayPair.value)
                } else {
                    BodyMatchMinArray(null, -matchArrayPair.value)
                }
                val resultMatch = buildMatch(splitPath(matchArrayPair.key), startElement)
                startingPoints.addMatch(resultMatch)
            }
            matchRegexes.forEach { matchRegexPair ->
                val resultMatch = buildMatch(splitPath(matchRegexPair.key), BodyMatchString(matchRegexPair.value))
                startingPoints.addMatch(resultMatch)
            }
            if (startingPoints.size > 1) {
                throw PactBuildException("There was more than one root defined by @MatchBody annotations:\n${startingPoints.joinToString(separator = "\n")}")
            }
            return startingPoints.firstOrNull()
        }

        private fun splitPath(path: String): List<String> {
            val pathElements = path.split('.')
            if (pathElements.isEmpty()) {
                throw PactBuildException("The path on a @MatchBody may not be empty. It must be separated by '.' characters.")
            }
            if (pathElements.first() != "$") {
                throw PactBuildException("The path on a @MatchBody must start with the root specified by the '$' character. It must be separated by '.' characters.")
            }
            return pathElements.reversed()
        }

        private fun buildMatch(pathElements: List<String>, startElement: BodyMatchElement): BodyMatchElement {
            return pathElements.fold(startElement) { lastMatch, pathElement ->
                when {
                    pathElement.startsWith("[") -> BodyMatchAbsoluteArray(lastMatch)
                    pathElement == "$" -> lastMatch
                    else -> BodyMatchObject(hashMapOf(Pair(pathElement, lastMatch)))
                }
            }
        }

        private fun LinkedList<BodyMatchElement>.addMatch(newMatch: BodyMatchElement) {
            firstOrNull { it.equalsType(newMatch) }?.mergeFrom(newMatch) ?: add(newMatch)
        }
    }
}