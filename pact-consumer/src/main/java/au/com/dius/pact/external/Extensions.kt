package au.com.dius.pact.external

import android.util.LruCache
import au.com.dius.pact.model.BasePact.Companion.jsonParser
import com.google.gson.JsonElement
import okhttp3.mockwebserver.RecordedRequest

val jsonCache = LruCache<String, JsonElement>(20)

@Synchronized
fun RecordedRequest.parseBodyToJson(): JsonElement {
    val inputBody = body.inputStream().use { it.reader().readText() }
    val resolvedItem = jsonCache[inputBody]
    if (resolvedItem != null) { return resolvedItem }
    val element = body.inputStream().use { jsonParser.parse(inputBody) }
    synchronized(jsonCache) {
        val resolvedItemNew = jsonCache[inputBody]
        if (resolvedItemNew != null) { return resolvedItemNew }
        jsonCache.put(inputBody, element)
    }
    return element
}