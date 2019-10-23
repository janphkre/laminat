package au.com.dius.pact.external.util

import java.util.LinkedList

fun <T> Iterator<T>.toList(): List<T> {
    val list = LinkedList<T>()
    while (hasNext()) {
        list.addLast(next())
    }
    return list
}