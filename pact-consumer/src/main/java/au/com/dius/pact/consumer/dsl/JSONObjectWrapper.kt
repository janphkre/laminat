package au.com.dius.pact.consumer.dsl

import org.json.JSONObject

/**
 * This wrapper swallows "unhandled" JsonExceptions
 */
class JSONObjectWrapper {

    fun put(rootName: String, body: Any) {
        if(body is JSONObjectWrapper) {
            jsonObject.put(rootName, body.jsonObject)
            return
        }
        jsonObject.put(rootName, body)
    }

    val jsonObject = JSONObject()

    override fun toString(): String {
        return jsonObject.toString()
    }
}