package au.com.dius.pact.external.json

fun jsonObject(vararg entries: Pair<String, Any?>): Json {
    return Json.Object(
        entries.map { it.first to json(it.second) }.toMap(mutableMapOf())
    )
}

fun jsonArray(vararg entries: Any?) : Json {
    return Json.Array(
        entries.mapTo(mutableListOf()) { json(it) }
    )
}

fun json(element: Any?): Json {
    return Json.wrapInJson(element)
}