package au.com.dius.pact.external

import au.com.dius.pact.model.BasePact.Companion.jsonParser
import com.google.gson.JsonElement
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

val jsonCache = WeakHashMap<RecordedRequest, JsonElement>()

@Synchronized
fun RecordedRequest.parseBodyToJson(): JsonElement {
    val resolvedItem = synchronized(jsonCache) { jsonCache[this] }
    if(resolvedItem != null) { return resolvedItem }
    synchronized(this) {
        val resolvedItemNew = synchronized(jsonCache) { jsonCache[this] }
        if(resolvedItemNew != null) { return resolvedItemNew }
        val element = body.inputStream().use { jsonParser.parse(it.reader()) }
        synchronized(jsonCache) { jsonCache.put(this, element) }
        return element
    }
}