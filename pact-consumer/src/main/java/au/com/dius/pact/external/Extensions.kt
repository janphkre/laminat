package au.com.dius.pact.external

import au.com.dius.pact.model.BasePact.Companion.jsonParser
import com.google.gson.JsonElement
import okhttp3.mockwebserver.RecordedRequest
import java.util.WeakHashMap

val jsonCache = WeakHashMap<RecordedRequest, JsonElement>()

@Synchronized
fun RecordedRequest.parseBodyToJson(): JsonElement {
    val resolvedItem = jsonCache[this]
    if (resolvedItem != null) { return resolvedItem }
    synchronized(this) {
        val resolvedItemNew = jsonCache[this]
        if (resolvedItemNew != null) { return resolvedItemNew }
        val element = body.inputStream().use { jsonParser.parse(it.reader()) }
        jsonCache.put(this, element)
        return element
    }
}