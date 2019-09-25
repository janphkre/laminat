package okhttp3

class RetrofitPactRequestWithParams(
    requestBuilder: Request.Builder
) {
    val method: String = requestBuilder.method
    val relativePath: String = requestBuilder.url!!.encodedPath()
    val query: List<Pair<String, String>> = requestBuilder.url!!.let { it.queryParameterNames().mapIndexed { index, name -> Pair(name, it.queryParameterValue(index)) } }
    val headers: List<Pair<String, String>> = requestBuilder.headers.namesAndValues.zipWithNext { a, b -> Pair(a, b) }
    val body: RequestBody? = requestBuilder.body
}