package au.com.dius.pact.pactbroker

import au.com.dius.pact.provider.broker.com.github.kittinunf.result.Result
import com.github.salomonbrys.kotson.*
import com.google.common.net.UrlEscapers
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.net.URI

/**
 * HAL client base class
 */
abstract class HalClientBase @JvmOverloads constructor(val baseUrl: String,
                                                       var options: Map<String, Any> = mapOf()) :
    IHalClient {

    var httpClient: CloseableHttpClient? = null
    var pathInfo: JsonElement? = null
    var lastUrl: String? = null

    override fun postJson(url: String, body: String) = postJson(url, body, null)

    override fun postJson(url: String, body: String,
                          handler: ((status: Int, response: CloseableHttpResponse) -> Boolean)?): Result<Boolean, Exception> {
        val client = setupHttpClient()

        return Result.of {
            val httpPost = HttpPost(url)
            httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
            httpPost.entity = StringEntity(body, ContentType.APPLICATION_JSON)

            client.execute(httpPost).use {
                if (handler != null) {
                    handler(it.statusLine.statusCode, it)
                } else {
                    it.statusLine.statusCode < 300
                }
            }
        }
    }

    open fun setupHttpClient(): CloseableHttpClient {
        if (httpClient == null) {
            val builder = HttpClients.custom().useSystemProperties()
            if (options["authentication"] is List<*>) {
                val authentication = options["authentication"] as List<*>
                val scheme = authentication.first().toString().toLowerCase()
                when (scheme) {
                    "basic" -> {
                        if (authentication.size > 2) {
                            val credsProvider = BasicCredentialsProvider()
                            val uri = URI(baseUrl)
                            credsProvider.setCredentials(
                                AuthScope(uri.host, uri.port),
                                UsernamePasswordCredentials(authentication[1].toString(), authentication[2].toString())
                            )
                            builder.setDefaultCredentialsProvider(credsProvider)
                        } else {
                            System.err.println("W/$HALCLIENT Basic authentication requires a username and password, ignoring.")
                        }
                    }
                    else -> System.err.println("W/$HALCLIENT Hal client Only supports basic authentication, got '$scheme', ignoring.")
                }
            } else if (options.containsKey("authentication")) {
                System.err.println("W/$HALCLIENT  Authentication options needs to be a list of values, ignoring.")
            }

            httpClient = builder.build()
        }

        return httpClient!!
    }

    override fun navigate(options: Map<String, Any>, link: String): IHalClient {
        pathInfo = pathInfo ?: fetch(ROOT)
        pathInfo = fetchLink(link, options)
        return this
    }

    override fun navigate(link: String) = navigate(mapOf(), link)

    override fun fetch(path: String) = fetch(path, true)

    override fun fetch(path: String, encodePath: Boolean): JsonElement {
        lastUrl = path
        val response = getJson(path, encodePath)
        when (response) {
            is Result.Success -> return response.value
            is Result.Failure -> throw response.error
        }
    }

    private fun getJson(path: String, encodePath: Boolean = true): Result<JsonElement, Exception> {
        setupHttpClient()
        return Result.of {
            val httpGet = HttpGet(buildUrl(path, encodePath))
            httpGet.addHeader("Content-Type", "application/json")
            httpGet.addHeader("Accept", "application/hal+json, application/json")

            val response = httpClient!!.execute(httpGet)
            if (response.statusLine.statusCode < 300) {
                val contentType = ContentType.getOrDefault(response.entity)
                if (isJsonResponse(contentType)) {
                    return@of JsonParser().parse(EntityUtils.toString(response.entity))
                } else {
                    throw InvalidHalResponse("Expected a HAL+JSON response from the pact broker, but got '$contentType'")
                }
            } else {
                when (response.statusLine.statusCode) {
                    404 -> throw NotFoundHalResponse("No HAL document found at path '$path'")
                    else -> throw RequestFailedException("Request to path '$path' failed with response '${response.statusLine}'")
                }
            }
        }
    }

    @JvmOverloads
    fun buildUrl(url: String, encodePath: Boolean = true): URI {
        val match = URL_REGEX.matchEntire(url)
        return if (match != null) {
            val (scheme, host, port, path) = match.destructured
            val builder = URIBuilder().setScheme(scheme).setHost(host)
            if (port.isNotEmpty()) {
                builder.port = port.substring(1).toInt()
            }
            if (encodePath) {
                builder.setPath(path).build()
            } else {
                URI(builder.build().toString() + path)
            }
        } else {
            if (encodePath) {
                URIBuilder(baseUrl).setPath(url).build()
            } else {
                URI(baseUrl + url)
            }
        }
    }

    private fun isJsonResponse(contentType: ContentType) = contentType.mimeType == "application/json" ||
            contentType.mimeType == "application/hal+json"

    private fun fetchLink(link: String, options: Map<String, Any>): JsonElement {
        if (pathInfo?.nullObj?.get("_links") == null) {
            throw InvalidHalResponse(
                "Expected a HAL+JSON response from the pact broker, but got " +
                        "a response with no '_links'. URL: '$baseUrl', LINK: '$link'"
            )
        }

        val links = pathInfo!!["_links"]
        if (links.isJsonObject) {
            if (!links.obj.has(link)) {
                throw InvalidHalResponse(
                    "Link '$link' was not found in the response, only the following links where " +
                            "found: ${links.obj.keys()}. URL: '$baseUrl', LINK: '$link'"
                )
            }
            val linkData = links[link]

            if (linkData.isJsonArray) {
                if (options.containsKey("name")) {
                    val linkByName = linkData.asJsonArray.find { it.isJsonObject && it["name"] == options["name"] }
                    return if (linkByName != null && linkByName.isJsonObject && linkByName["templated"].isJsonPrimitive &&
                        linkByName["templated"].bool) {
                        this.fetch(parseLinkUrl(linkByName["href"].toString(), options), false)
                    } else if (linkByName != null && linkByName.isJsonObject) {
                        this.fetch(linkByName["href"].string)
                    } else {
                        throw InvalidNavigationRequest(
                            "Link '$link' does not have an entry with name '${options["name"]}'. " +
                                    "URL: '$baseUrl', LINK: '$link'"
                        )
                    }
                } else {
                    throw InvalidNavigationRequest(
                        "Link '$link' has multiple entries. You need to filter by the link name. " +
                                "URL: '$baseUrl', LINK: '$link'"
                    )
                }
            } else if (linkData.isJsonObject) {
                return if (linkData.obj.has("templated") && linkData["templated"].isJsonPrimitive &&
                    linkData["templated"].bool) {
                    fetch(parseLinkUrl(linkData["href"].string, options), false)
                } else {
                    fetch(linkData["href"].string)
                }
            } else {
                throw InvalidHalResponse(
                    "Expected link in map form in the response, but " +
                            "found: $linkData. URL: '$baseUrl', LINK: '$link'"
                )
            }
        } else {
            throw InvalidHalResponse(
                "Expected a map of links in the response, but " +
                        "found: $links. URL: '$baseUrl', LINK: '$link'"
            )
        }
    }

    fun parseLinkUrl(href: String, options: Map<String, Any>): String {
        var result = ""
        var match = URL_TEMPLATE_REGEX.find(href)
        var index = 0
        while (match != null) {
            val start = match.range.start - 1
            if (start >= index) {
                result += href.substring(index..start)
            }
            index = match.range.endInclusive + 1
            val (key) = match.destructured
            result += encodePathParameter(options, key, match.value)

            match = URL_TEMPLATE_REGEX.find(href, index)
        }

        if (index < href.length) {
            result += href.substring(index)
        }
        return result
    }

    private fun encodePathParameter(options: Map<String, Any>, key: String, value: String) =
        UrlEscapers.urlPathSegmentEscaper().escape(options[key]?.toString() ?: value)

    fun initPathInfo() {
        pathInfo = pathInfo ?: fetch(ROOT)
    }

    override fun uploadJson(path: String, bodyJson: String) = uploadJson(path, bodyJson, null, true)

    override fun uploadJson(path: String, bodyJson: String, closure: BiFunction<String, String, Any?>?) =
        uploadJson(path, bodyJson, closure, true)

    override fun uploadJson(path: String, bodyJson: String, closure: BiFunction<String, String, Any?>?,
                            encodePath: Boolean): Any? {
        val client = setupHttpClient()
        val httpPut = HttpPut(buildUrl(path, encodePath))
        httpPut.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
        httpPut.entity = StringEntity(bodyJson, ContentType.APPLICATION_JSON)

        client.execute(httpPut).use {
            return when {
                it.statusLine.statusCode < 300 -> {
                    EntityUtils.consume(it.entity)
                    closure?.apply("OK", it.statusLine.toString())
                }
                it.statusLine.statusCode == 409 -> {
                    val body = it.entity.content.bufferedReader().readText()
                    closure?.apply("FAILED",
                        "${it.statusLine.statusCode} ${it.statusLine.reasonPhrase} - $body")
                }
                else -> {
                    if (closure != null) {
                        val body = it.entity.content.bufferedReader().readText()
                        handleFailure(it, body, closure)
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun handleFailure(resp: HttpResponse, body: String?, closure: BiFunction<String, String, Any?>): Any? {
        if (resp.entity.contentType != null) {
            val contentType = ContentType.getOrDefault(resp.entity)
            if (isJsonResponse(contentType)) {
                var error = "Unknown error"
                if (body != null) {
                    val jsonBody = JsonParser().parse(body)
                    if (jsonBody != null && jsonBody.obj.has("errors")) {
                        if (jsonBody["errors"].isJsonArray) {
                            error = jsonBody["errors"].asJsonArray.joinToString(", ") { it.asString }
                        } else if (jsonBody["errors"].isJsonObject) {
                            error = jsonBody["errors"].asJsonObject.entrySet().joinToString(", ") {
                                if (it.value.isJsonArray) {
                                    "${it.key}: ${it.value.array.joinToString(", ") { it.asString }}"
                                } else {
                                    "${it.key}: ${it.value.asString}"
                                }
                            }
                        }
                    }
                }
                return closure.apply("FAILED", "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} - $error")
            } else {
                return closure.apply("FAILED", "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} - $body")
            }
        } else {
            return closure.apply("FAILED", "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} - $body")
        }
    }

    companion object {
        const val HALCLIENT = "HalClient:"
        const val ROOT = "/"
        val URL_TEMPLATE_REGEX = Regex("\\{(\\w+)\\}")
        val URL_REGEX = Regex("([^:]+):\\/\\/([^\\/:]+)(:\\d+)?(.*)")
    }
}