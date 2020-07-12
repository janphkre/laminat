package au.com.dius.pact.model.util

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

    private val pushedBackElements = ArrayList<T>()

    override fun hasNext(): Boolean {
        return pushedBackElements.isNotEmpty() || wrappedIterator.hasNext()
    }

    override fun next(): T {
        return if (pushedBackElements.isNotEmpty()) {
            pushedBackElements.removeAt(pushedBackElements.size - 1)
        } else {
            wrappedIterator.next()
        }
    }

    fun pushback(element: T) {
        pushedBackElements.add(element)
    }
}