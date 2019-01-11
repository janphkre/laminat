package au.com.dius.pact.consumer.dsl

import org.json.JSONObject

class JSONObjectWrapper {

    fun put(rootName: String, body: Any) {
        jsonObject.put(rootName, body)
    }

    private val jsonObject = JSONObject()
}