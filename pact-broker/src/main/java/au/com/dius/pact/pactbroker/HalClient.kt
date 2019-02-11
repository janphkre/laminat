package au.com.dius.pact.pactbroker

import com.google.gson.JsonElement
import org.apache.commons.collections4.Closure

/*class HalClient: HalClientBase {

    constructor(baseUrl: String, options: Map<String, Any>): super(baseUrl, options)

    constructor(baseUrl: String): super(baseUrl)

    fun methodMissing(name: String, conversion: ((JsonElement) -> JsonElement)?): List<JsonElement> {
        super.initPathInfo()
        if(pathInfo?.isJsonObject == true) {
            val links = pathInfo?.asJsonObject?.get("_links")
            if(links?.isJsonObject == true) {
                val matchingLink = links.asJsonObject[name]
                if (matchingLink != null) {
                    if (conversion != null) {
                        if (matchingLink.isJsonArray) {
                            return matchingLink.asJsonArray.map { conversion.invoke(it) }
                        }
                        return listOf(conversion.invoke(matchingLink))
                    }
                    return listOf(matchingLink)
                }
            }
        }
        throw MissingMethodException(name, this.class, args)
    }

    override fun linkUrl(name: String): String {
        TODO("not implemented")
    }

    override fun forAll(linkName: String, closure: Closure<Map<String, Any>>) {
        TODO("not implemented")
    }
}*/