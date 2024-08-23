package au.com.dius.pact.external.util

import java.util.LinkedList

/**
 * Short utility method for converting iterators to lists
 * to completely remove the dependency on
 * org.apache.commons.collections4.IteratorUtils
 *
 * @author Jan Philip Kretzschmar
 */
fun <T> Iterator<T>.toList(): List<T> {
    val list = LinkedList<T>()
    while (hasNext()) {
        list.addLast(next())
    }
    return list
}