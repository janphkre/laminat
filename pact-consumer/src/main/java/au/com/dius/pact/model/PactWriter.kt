package au.com.dius.pact.model

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
     * @param pactSpecVersion Pact version to use to control writing
     */
    @JvmStatic
    @JvmOverloads
    fun writePact(pact: Pact, writer: PrintWriter, pactSpecVersion: PactSpecVersion = PactSpecVersion.V3) {
        val sortedPact = pact.sortInteractions()
        val jsonData = sortedPact.toMap(pactSpecVersion)
        val gson = GsonBuilder().setPrettyPrinting().create()
        gson.toJson(jsonData, writer)
    }
}