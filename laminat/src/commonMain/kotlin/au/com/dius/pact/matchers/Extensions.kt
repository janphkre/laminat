package au.com.dius.pact.matchers

fun <T, R> List<T>.zipFirstNullable(other: Iterable<R>): List<Pair<T?, R>> {
    val first = iterator()
    val second = other.iterator()
    val list = ArrayList<Pair<T?, R>>(maxOf(size, other.count()))
    while (second.hasNext()) {
        val firstValue = if (first.hasNext()) first.next() else null
        list.add(firstValue to second.next())
    }
    return list
}