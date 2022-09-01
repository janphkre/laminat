package au.com.dius.pact.matchers

import com.google.gson.JsonElement
import com.google.gson.JsonParser
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

fun Reader?.readJson(): JsonElement {
    return JsonParser.parseReader(this)
}

fun ByteArray?.readJson(): JsonElement {
    return this?.toUtf8Reader().readJson()
}

fun String.toUtf8ByteArray(): ByteArray {
    return this.toByteArray(Charsets.UTF_8)
}