package au.com.dius.pact.external.util

import java.util.Stack

/**
 * Similar to org.apache.commons.collections4.iterators.PushbackIterator
 */
class PushbackIterator<T>(
    private val wrappedIterator: Iterator<T>
) : Iterator<T> {
    private val pushedBackElements = Stack<T>()

    override fun hasNext(): Boolean {
        return pushedBackElements.isNotEmpty() || wrappedIterator.hasNext()
    }

    override fun next(): T {
        return if (pushedBackElements.isNotEmpty()) {
            pushedBackElements.pop()
        } else {
            wrappedIterator.next()
        }
    }

    fun pushback(element: T) {
        pushedBackElements.push(element)
    }
}