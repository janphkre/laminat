package au.com.dius.pact.external.util

fun <K, V, R, S> MutableMap<K, V>.mapKeyValues(lambda: (Map.Entry<K, V>) -> Pair<R, S>): MutableMap<R, S> {
    val result = LinkedHashMap<R, S>(this.size)
    this.forEach { entry ->
        val resultEntry = lambda.invoke(entry)
        result[resultEntry.first] = resultEntry.second
    }
    return result
}