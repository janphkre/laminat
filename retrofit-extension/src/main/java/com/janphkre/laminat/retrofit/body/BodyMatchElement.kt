package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.external.PactBuildException
import com.janphkre.laminat.retrofit.annotations.body.IMatchArray
import com.janphkre.laminat.retrofit.annotations.body.MaxArray
import com.janphkre.laminat.retrofit.annotations.body.MinArray
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
    abstract fun finalize()

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
                throw PactBuildException("Found conflicting types while merging $other into the object $this.") // ktlint-disable max-line-length
            }
            return this
        }

        override fun finalize() {
            entries.forEach {
                it.value.finalize()
            }
        }

        override fun toString(): String {
            return entries.entries.joinToString(
                separator = ",",
                prefix = "{",
                postfix = "}",
                truncated = "…",
                limit = MAX_OBJECT_STRING_LENGTH
            ) {
                "${it.key}:${it.value}"
            }
        }
    }

    abstract class BodyMatchArray : BodyMatchElement() {
        abstract fun at(index: Int): BodyMatchElement?
    }

    class BodyMatchAbsoluteArray(
        internal val elements: MutableMap<Int, BodyMatchElement>
    ) : BodyMatchArray() {

        override fun at(index: Int): BodyMatchElement? {
            return elements[index] ?: elements[-1]
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchAbsoluteArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            if (other is BodyMatchAbsoluteArray) {
                other.elements.forEach {
                    elements[it.key] = elements[it.key]?.mergeFrom(it.value) ?: it.value
                }
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the array $this.") // ktlint-disable max-line-length
            }
            return this
        }

        override fun finalize() {
            val genericElement = elements.remove(-1)
            if (genericElement != null) {
                elements.mapValues {
                    it.value.mergeFrom(genericElement)
                }
                elements[-1] = genericElement
            }
        }

        override fun toString(): String {
            return "[${elements.size}]"
        }
    }

    class BodyMatchMinArray(
        private var genericElement: BodyMatchElement?,
        internal val minCount: Int
    ) : BodyMatchArray() {

        override fun at(index: Int): BodyMatchElement? {
            return genericElement
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchMinArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            genericElement = when (other) {
                is BodyMatchMinArray -> {
                    if (minCount != other.minCount) {
                        throw PactBuildException("Found conflicting minCount $minCount & ${other.minCount} while merging $other into the min array $this.") // ktlint-disable max-line-length
                    }
                    genericElement?.mergeFrom(other.genericElement) ?: other.genericElement
                }
                is BodyMatchAbsoluteArray -> {
                    if (other.elements.size > 1) {
                        throw PactBuildException("Only one absolute element of $other can be used in this generic element $this!") // ktlint-disable max-line-length
                    }
                    val otherValue = other.elements.entries.firstOrNull()?.value
                    if (otherValue != null) {
                        genericElement?.mergeFrom(otherValue) ?: otherValue
                    } else {
                        genericElement
                    }
                }
                else -> throw PactBuildException("Found conflicting types while merging $other into the min array $this.") // ktlint-disable max-line-length
            }
            return this
        }

        override fun finalize() {
            genericElement?.finalize()
        }

        override fun toString(): String {
            return "[$genericElement]"
        }
    }

    class BodyMatchMaxArray(
        private var genericElement: BodyMatchElement?,
        internal val maxCount: Int
    ) : BodyMatchArray() {

        override fun at(index: Int): BodyMatchElement? {
            return genericElement
        }

        override fun equalsType(other: BodyMatchElement): Boolean {
            return other is BodyMatchMaxArray
        }

        override fun mergeFrom(other: BodyMatchElement?): BodyMatchElement {
            genericElement = when (other) {
                is BodyMatchMaxArray -> {
                    if (maxCount != other.maxCount) {
                        throw PactBuildException("Found conflicting maxCount $maxCount & ${other.maxCount} while merging $other into the max array $this.") // ktlint-disable max-line-length
                    }
                    genericElement?.mergeFrom(other.genericElement) ?: other.genericElement
                }
                is BodyMatchAbsoluteArray -> {
                    if (other.elements.size > 1) {
                        throw PactBuildException("Only one absolute element of $other can be used in this generic element $this!") // ktlint-disable max-line-length
                    }
                    val otherValue = other.elements.entries.firstOrNull()?.value
                    if (otherValue != null) {
                        genericElement?.mergeFrom(otherValue) ?: otherValue
                    } else {
                        genericElement
                    }
                }
                else -> throw PactBuildException("Found conflicting types while merging $other into the max array $this.") // ktlint-disable max-line-length
            }
            return this
        }

        override fun finalize() {
            genericElement?.finalize()
        }

        override fun toString(): String {
            return "[$genericElement]"
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
                throw PactBuildException("Merging of regexes is unsupported. Tried to merge \"$regex\" and \"${other.regex}\"") // ktlint-disable max-line-length
            } else {
                throw PactBuildException("Found conflicting types while merging $other into the regex \"$regex\".") // ktlint-disable max-line-length
            }
        }

        override fun finalize() { }

        override fun toString(): String {
            if (regex.length < MAX_ITEM_STRING_LENGTH) {
                return "\"$regex\""
            }
            return "\"${regex.take(MAX_ITEM_STRING_LENGTH - 1)}…\""
        }
    }

    companion object {

        private const val MAX_ITEM_STRING_LENGTH = 7
        private const val MAX_OBJECT_STRING_LENGTH = 21

        fun from(
            matchRegexes: Map<String, String>,
            matchArrays: Map<String, IMatchArray>
        ): BodyMatchElement? {
            val startingPoints = LinkedList<BodyMatchElement>()
            matchArrays.forEach { matchArrayPair ->
                val startElement = when (val arrayValue = matchArrayPair.value) {
                    is MaxArray -> BodyMatchMaxArray(null, arrayValue.maxCount)
                    is MinArray -> BodyMatchMinArray(null, arrayValue.minCount)
                    else -> throw PactBuildException(
                        "Unsupported array type ${arrayValue.javaClass.name}"
                    )
                }
                val resultMatch = buildMatch(splitPath(matchArrayPair.key), startElement)
                startingPoints.addMatch(resultMatch)
            }
            matchRegexes.forEach { matchRegexPair ->
                val resultMatch = buildMatch(
                    splitPath(matchRegexPair.key),
                    BodyMatchString(matchRegexPair.value)
                )
                startingPoints.addMatch(resultMatch)
            }
            if (startingPoints.size > 1) {
                throw PactBuildException("There was more than one root defined by @MatchBody annotations:\n${startingPoints.joinToString(separator = "\n")}") // ktlint-disable max-line-length
            }
            return startingPoints.firstOrNull() // ?.apply { finalize() }
        }

        private fun splitPath(path: String): List<String> {
            val pathElements = path.split('.', '[')
            if (pathElements.isEmpty()) {
                throw PactBuildException("The path on a @MatchBody may not be empty. It must be separated by '.' characters.") // ktlint-disable max-line-length
            }
            if (pathElements.first() != "$") {
                throw PactBuildException("The path on a @MatchBody must start with the root specified by the '$' character. It must be separated by '.' characters.") // ktlint-disable max-line-length
            }
            return pathElements.reversed()
        }

        private fun buildMatch(
            pathElements: List<String>,
            startElement: BodyMatchElement
        ): BodyMatchElement {
            return pathElements.fold(startElement) { lastMatch, pathElement ->
                when {
                    pathElement.endsWith("]") -> {
                        if (pathElement.length < 2) {
                            throw PactBuildException("The path element $pathElement does not contain an index. Wildcards are allowed.") // ktlint-disable max-line-length
                        }
                        val pathValue = pathElement.substring(0, pathElement.length - 1)
                        val itemIndex = if (pathValue == "*") {
                            -1
                        } else {
                            pathValue.toIntOrNull() ?: throw PactBuildException("The path element $pathElement does not contain a integer index. Wildcards are allowed.") // ktlint-disable max-line-length
                        }
                        BodyMatchAbsoluteArray(
                            mutableMapOf(
                                Pair(itemIndex, lastMatch)
                            )
                        )
                    }
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
