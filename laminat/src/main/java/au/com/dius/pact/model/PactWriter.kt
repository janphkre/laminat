package au.com.dius.pact.model

import au.com.dius.pact.external.json.Json
import au.com.dius.pact.external.json.JsonGsonAdapter
import com.google.gson.GsonBuilder
import java.io.PrintWriter

/**
 * Class to write out a au.com.dius.pact to a file
 */
object PactWriter {

    /**
     * Writes out the au.com.dius.pact to the provided au.com.dius.pact file
     * @param pact Pact to write
     * @param writer Writer to write out with
     * @param serializationConfig Configuration to use to control writing
     */
    @JvmStatic
    @JvmOverloads
    fun writePact(pact: Pact, writer: PrintWriter, serializationConfig: PactSerializationConfig = PactSerializationConfig(PactSpecVersion.V3, null)) {
        val sortedPact = pact.sortInteractions()
        val jsonData = sortedPact.toMap(serializationConfig)
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Json::class.java, JsonGsonAdapter())
            .create()
        gson.toJson(jsonData, writer)
    }
}