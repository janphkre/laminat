package au.com.dius.pact.external.util

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader
import org.apache.http.Consts

fun ByteArray.toUtf8Reader(): Reader {
    val stream = ByteArrayInputStream(this)
    return InputStreamReader(stream, Consts.UTF_8)
}

fun ByteArray.toUtf8String(): String {
    return toUtf8Reader().readText()
}

fun String.toUtf8ByteArray(): ByteArray {
    return this.toByteArray(Charsets.UTF_8)
}