package au.com.dius.pact.pactbroker

import au.com.dius.pact.provider.broker.com.github.kittinunf.result.Result
import com.google.gson.JsonElement
import org.apache.commons.collections4.Closure
import org.apache.http.client.methods.CloseableHttpResponse

/**
 * Interface to a HAL Client
 */
interface IHalClient {
  /**
   * Navigates the URL associated with the given link using the current HAL document
   * @param options Map of key-value pairs to use for parsing templated links
   * @param link Link name to navigate
   */
  fun navigate(options: Map<String, Any> = mapOf(), link: String): IHalClient

  /**
   * Navigates the URL associated with the given link using the current HAL document
   * @param link Link name to navigate
   */
  fun navigate(link: String): IHalClient

  /**
   * Returns the HREF of the named link from the current HAL document
   */
  fun linkUrl(name: String): String

  /**
   * Calls the closure with a Map of attributes for all links associated with the link name
   * @param linkName Name of the link to loop over
   * @param closure Closure to invoke with the link attributes
   */
  fun forAll(linkName: String, closure: Closure<Map<String, Any>>)

  /**
   * Upload the JSON document to the provided path, using a PUT request
   * @param path Path to upload the document
   * @param bodyJson JSON contents for the body
   */
  fun uploadJson(path: String, bodyJson: String): Any?

  /**
   * Upload the JSON document to the provided path, using a PUT request
   * @param path Path to upload the document
   * @param bodyJson JSON contents for the body
   * @param closure Closure that will be invoked with details about the response. The result from the closure will be
   * returned.
   */
  fun uploadJson(path: String, bodyJson: String, closure: BiFunction<String, String, Any?>?): Any?

  /**
   * Upload the JSON document to the provided path, using a PUT request
   * @param path Path to upload the document
   * @param bodyJson JSON contents for the body
   * @param closure Closure that will be invoked with details about the response. The result from the closure will be
   * returned.
   * @param encodePath If the path must be encoded beforehand.
   */
  fun uploadJson(path: String, bodyJson: String, closure: BiFunction<String, String, Any?>?, encodePath: Boolean): Any?

  /**
   * Upload the JSON document to the provided URL, using a POST request
   * @param url Url to upload the document to
   * @param body JSON contents for the body
   * @return Returns a Success result object with a boolean value to indicate if the request was successful or not. Any
   * exception will be wrapped in a Failure
   */
  fun postJson(url: String, body: String): Result<Boolean, Exception>

  /**
   * Upload the JSON document to the provided URL, using a POST request
   * @param url Url to upload the document to
   * @param body JSON contents for the body
   * @param handler Response handler
   * @return Returns a Success result object with the boolean value returned from the handler closure. Any
   * exception will be wrapped in a Failure
   */
  fun postJson(url: String, body: String, handler: ((status: Int, response: CloseableHttpResponse) -> Boolean)?): Result<Boolean, Exception>

  /**
   * Fetches the HAL document from the provided path
   * @param path The path to the HAL document. If it is a relative path, it is relative to the base URL
   * @param encodePath If the path should be encoded to make a valid URL
   */
  fun fetch(path: String, encodePath: Boolean): JsonElement

  /**
   * Fetches the HAL document from the provided path
   * @param path The path to the HAL document. If it is a relative path, it is relative to the base URL
   */
  fun fetch(path: String): JsonElement
}
