package au.com.dius.pact.util

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

fun Char.isDigit() : Boolean {
    return Extensions.digitCharRange.contains(this)
}

fun Char.isLetterOrDigit() : Boolean {
    return Extensions.upperCaseCharRange.contains(this) || Extensions.lowerCaseCharRange.contains(this) || isDigit()
}

object Extensions {
    internal val digitCharRange = '\u0030'..'\u0039' // '0'..'9'
    internal val upperCaseCharRange = ('\u0041'..'\u005A') // 'A'..'Z'
    internal val lowerCaseCharRange = ('\u0061'..'\u007A') // 'a'..'z'
}