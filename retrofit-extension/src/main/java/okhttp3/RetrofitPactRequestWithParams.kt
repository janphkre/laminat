package okhttp3

class RetrofitPactRequestWithParams(
    requestBuilder: Request
) {
    val method: String = requestBuilder.method
    val relativePath: String = requestBuilder.url.encodedPath
    val query: List<Pair<String, String?>> = requestBuilder.url.let {
        it.queryParameterNames.mapIndexed { index, name ->
            Pair(name, it.queryParameterValue(index))
        }
    }
    val headers: List<Pair<String, String>>
    val body: RequestBody? = requestBuilder.body

    init {
        val size = requestBuilder.headers.size
        headers = ArrayList<Pair<String, String>>(size).also { headers ->
            for (i in 0 until size) {
                headers.add(Pair(requestBuilder.headers.name(i), requestBuilder.headers.value(i)))
            }
        }
    }
}
