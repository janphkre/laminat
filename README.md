# laminat

[![](https://jitpack.io/v/janphkre/laminat.svg)](https://jitpack.io/#janphkre/laminat)

Lightweight Android version of Pact. Enables consumer driven contract testing, providing DSL for the consumer project.

The pact library is meant for consumer driven contracts. This means that you, as a consumer, define the content and form of a request or message towards a provider.
In order to maintain consistency between the consumer and the provider pact defines a dsl which can be serialized and deserialized into and from json.
Therefore the consumer defines the pact and publishes it to the provider.
From this pact a mocked/stubbed provider can be definied on the consumer side and a mocked/stubbed consumer can be defined on the provider side which makes it possible to ensure the integration for both sides of the contract.

This is a lightweight adaption of [pact-jvm7.0][2] for the Android-vm. It is only meant for the consumer side of pact,
therefore it only contains pact creation, serialization and consumer matching. This adaption was mostly created to get rid of the spring framework and additonal unused HTTP-Clients to maintain a small library on android so you can use a HttpLibrary of your choice. At the moment it comes bundled with the [OkHttp MockWebServer library][1].
Also, this lightweight adaption requires neither groovy, scala nor ruby anymore.

Get the library through jitpack:
```groovy
repositories {
    //...
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.janphkre:laminat:3.5.7'
}
```

### Pact creation
Just like the normal [pact-jvm project][2], the PactDSL can be used in this implementation.

```kotlin
val examplePact = ConsumerPactBuilder("exampleConsumer")
.hasPactWith("exampleProvider")
.given("ExampleState")
    .uponReceiving("GET firstExample")
        .path("/example/first")
        .method(HttpMethod.GET)
        .matchHeader("Accept-Language", "[a-z]{2}", "en")
        .matchQuery("exampleQuery", "\\d+(\\.\\d{1,6})?", "3.14")
        .willRespondWith()
            .status(200)
            .headers(defaultResponseHeaders)
            .body(PactDslJsonBody()
                .`object`("exampleField")
                    .numberType("exampleNumber", 1234)
                    .stringMatcher("exampleString", ".*", "Hello World!")
                .closeObject()
            )
.toPact()
```

More examples of the pact dsl can be found in the [pact jvm repository][2].
Also custom extensions could be written like such:

```kotlin
fun PactDslJsonBody.obj(name: String, initializer: PactDslJsonBody.() -> DslPart?): PactDslJsonBody {
    val result = this.`object`(name)
    initializer.invoke(result)
    result.closeObject()
    return this
}
```

Which then can be used in the dsl using it instead of `object` with a lambda.

### Pact server

A server which will respond to incoming requests as specified in a pact / multiple pacts can be specified as follows:

```kotlin
val mockServer = StatefullPactWebServer(allowUnexpectedKeys = false, pactErrorCode = 998)

mockServer.addPact(examplePact)
mockServer.setStates(listOf("ExampleState"))

/* ... Do your requests ... */

mockServer.teardown()

// Validate that all pact interactions have been called:
Assert.assertTrue(mockServer.validatePactsCompleted())
```

The creation of the web server requires only two variables.
First, you can specify if unexpected keys in incoming requests should be ignored. This should usually be set to false as you are a consumer
and should specify your interactions as complete as possible, since an interaction in a pact should always be contracting by example.
Second, you can specify the code that the server should return as an HTTP error code in the response that it gives.
It should be outside of the expected error code range in your consumer so you can easily distinguish an pact error from an expected server error.
When adding an interceptor to the async response chain you can crash the app / test by this for example, as you do not want to have your implementation deviate from the pact.

Also if you do not care about state, you can also use it state less:

```kotlin
val mockServer = StatelessPactWebServer(allowUnexpectedKeys = false, pactErrorCode = 998)

mockServer.addPact(examplePact)

/* ... Do your requests ... */

mockServer.teardown()

// Validate that all pact interactions have been called:
Assert.assertTrue(mockServer.validatePactsCompleted())
```

### Serialization
A single object au.com.dius.pact.external.PactJsonifier has been added which allows easy serialization of a list of pacts by merging them first and writing them into a file afterwards.
This step could be wrapped into a unit test instead of an instrumented test so the pact files are available on the host machine directly.
Also you a unit test should be added to check for conflicts between the interactions in one pact.

```kotlin
@Test
fun pact_merged_ToJson() {
    val pacts = Pacts.getAllPacts()
    PactJsonifier.generateJson(pacts, File("pacts"))
    Assert.assertTrue("Pact was not generated!", File("pacts/example_consumer:example_provider.json").exists())
}

@Test
fun pact_merged_NoConflicts() {
    val pacts = Pacts.getAllPacts()
    for (first in pacts) {
       val selfConflicts = first.conflictsWithSelf()
       Assert.assertTrue("Found self conflicts in pact: \n ${selfConflicts.joinToString("\n")}", selfConflicts.isEmpty())

       for (second in pacts) {
          if (first === second) {
              continue
          }
          Assert.assertTrue("Pacts are not compatible: $first; $second", first.compatibleTo(second))
          val otherConflicts = first.conflictsWith(second)
          Assert.assertTrue("Found conflicts between pacts: \n ${otherConflicts.joinToString("\n")}", otherConflicts.isEmpty())
       }
    }
}
```

[1]: https://github.com/square/okhttp
[2]: https://github.com/DiUS/pact-jvm/tree/v3.5.x-jre7
