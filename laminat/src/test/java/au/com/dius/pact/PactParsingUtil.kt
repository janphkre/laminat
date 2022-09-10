package au.com.dius.pact

import au.com.dius.pact.external.json.Json
import java.io.File

object PactParsingUtil {

    fun parseAssetToJson(assetName: String): Json {
        val content = readFileFromAssets(assetName)
        val result = parseContentToJson(content)
        (((result as Json.Object)["metadata"] as Json.Object)["pact-laminat-android"] as Json.Object)["version"] = Json.Primitive.StringPrimitive(BuildConfig.VERSION_NAME)
        return result
    }

    fun parseContentToJson(content: String): Json {
        return Json.parse(content)
    }

    private fun readFileFromAssets(assetName: String): String {
        return File("src/test/assets/$assetName").readText(Charsets.UTF_8).trim()
    }
}