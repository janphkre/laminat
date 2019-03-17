package au.com.dius.pact.matchers

import com.google.gson.internal.LazilyParsedNumber
import kotlin.reflect.KFunction

private fun isNumeric(method: KFunction<*>): Boolean {
    return try {
        method.call()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun LazilyParsedNumber.isInt(): Boolean {
    return isNumeric(::toInt)
}

fun LazilyParsedNumber.isLong(): Boolean {
    return isNumeric(::toLong)
}

fun LazilyParsedNumber.isFloat(): Boolean {
    return isNumeric(::toFloat)
}

fun LazilyParsedNumber.isDouble(): Boolean {
    return isNumeric(::toDouble)
}

fun LazilyParsedNumber.isChar(): Boolean {
    return isNumeric(::toChar)
}

fun LazilyParsedNumber.isByte(): Boolean {
    return isNumeric(::toByte)
}

fun LazilyParsedNumber.isShort(): Boolean {
    return isNumeric(::toShort)
}

fun LazilyParsedNumber.isNumber(): Boolean {
    return isInt() || isLong() || isFloat() || isDouble() || isChar() || isByte() || isShort()
}

fun <T, R> List<T>.zipFirstNullable(other: Iterable<R>): List<Pair<T?,R>> {
    val first = iterator()
    val second = other.iterator()
    val list = ArrayList<Pair<T?,R>>(maxOf(size, other.count()))
    while (second.hasNext()) {
        val firstValue = if(first.hasNext()) first.next() else null
        list.add(firstValue to second.next())
    }
    return list
}