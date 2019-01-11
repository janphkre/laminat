# laminat
Lightweight Android version of Pact. Enables consumer driven contract testing, providing DSL for the consumer project.

The pact library is meant for consumer driven contracts. This means that you, as a consumer, define the content and form of a request or message towards a provider.
In order to maintain consistency between the consumer and the provider pact defines a dsl which can be serialized and deserialized into and from json.
Therefore the consumer defines the pact and publishes it to the provider.
From this pact a mocked/stubbed provider can be definied on the consumer side and a mocked/stubbed consumer can be defined on the provider side which makess it possible to ensure the integration for both sides of the contract.

This is an early prototype of a lightweight adaption of pact-jvm7.0 for the Android-vm. It is only meant for the consumer side of pact,
therefore it only contains pact creation, serialization and consumer matching. This adaption was mostly created to get rid of the spring framework and additonal unused HTTPClients to maintain a small library on android so that you can use the HttpLibrary of your choice.
If you are a bit at loss on how to use this library check out the last section of this readme.

### Pact creation
Just like the normal pact-jvm project, the PactDSL can be used in this implementation.
As of now, regular expressions in the DSL are untested and are most likely not working, since the MatchingRules have no use in the consumer-matching right now.

### Serialization
A single object au.com.dius.pact.external.PactJsonifier has been added which allows easy serialization of a list of pacts by merging them first and writing them into a file afterwards.

### Consumer matching
The matching of the consumer side of pact can be used for json by implementing a subclass of JsonRequestPactMatcher.
The query, headers and custom additional MatcherRules have to be matched in that subclass, since they can get very specific.

### Usage
In order to use the mocked provider on your Android device, you most convieniently want a mock server running on your device where you can reply to incoming requests.
For this example we will go with [com.squareup.okhttp3:mockwebserver](https://github.com/square/okhttp).
The okHttp MockWebserver provides an dispatcher abstract class which allows to create a mock response to an incoming request:
```
val pactMatcher = object: JsonRequestPactMatcher<HttpUrl, Headers?>() {
  override fun Map<String, String>?.matchHeader(userHeaders: Headers?): Boolean {
        if((this?.size ?: 0) == 0 && (userHeaders?.size() ?: 0) == 0) {
            return true
        }
        if(this == null || userHeaders == null || this.size != userHeaders.size()) {
            return false
        }
        forEach {
            if(get(it.key) != it.value) {
                return false
            }
        }
        return true
    }

    override fun Map<String, List<String>>.matchQuery(userQuery: HttpUrl): Boolean {
        ...
    }

    override fun MatchingRules.matchRules(method: String, path: String, query: HttpUrl, headers: Headers?, body: String): Boolean {
        ...
    }
}

override fun dispatch(request: RecordedRequest?): MockResponse {
    if(request == null) {
        return MockResponse().setResponseCode(404)
    }
    val bodyString = request.body.inputStream().use {
        it.bufferedReader().readText()
    }
    val interaction = pactMatcher.findInteraction(pactList, currentProviderStates, request.method, request.path, request.requestUrl, request.headers, bodyString)
    return interaction?.response?.generateResponse()?.mapToMockResponse() ?: MockResponse().setResponseCode(404)
}

private fun Response.mapToMockResponse(): MockResponse {
    return MockResponse()
        .setResponseCode(this.status)
        .setHeaders(this.headers.mapToMockHeaders())
        .setBody(this.body.value ?: "")
}

private fun Map<String, String>?.mapToMockHeaders(): Headers {
    if(this == null) {
        return Headers.of()
    }
    return Headers.of(this)
}
```
