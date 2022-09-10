package au.com.dius.pact.external.util

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.Reader
import org.apache.http.Consts
import java.nio.charset.Charset

fun ByteArray.toReader(charset: Charset): Reader {
    val stream = ByteArrayInputStream(this)
    return InputStreamReader(stream, charset)
}

fun ByteArray.toUtf8String(): String {
    return toReader(Charsets.UTF_8).readText()
}
