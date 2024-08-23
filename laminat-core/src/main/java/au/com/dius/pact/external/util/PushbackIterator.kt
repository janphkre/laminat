package au.com.dius.pact.external.util

import java.util.Stack

/**
 * Iterator which allows to push back elements onto the iterator.
 * The pushed back elements are returned as a LIFO in next before returning
 * new elements from the wrapped iterator.
 * Used to completely replace dependency on
 * org.apache.commons.collections4.iterators.PushbackIterator
 *
 * @author Jan Philip Kretzschmar
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