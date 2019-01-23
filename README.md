# laminat
THIS IS STILL WORK IN PROGRESS, I AM CURRENTLY ADDING THE MATCHING TOGETHER WITH A OKHTTP MOCKWEBSERVER.

Lightweight Android version of Pact. Enables consumer driven contract testing, providing DSL for the consumer project.

The pact library is meant for consumer driven contracts. This means that you, as a consumer, define the content and form of a request or message towards a provider.
In order to maintain consistency between the consumer and the provider pact defines a dsl which can be serialized and deserialized into and from json.
Therefore the consumer defines the pact and publishes it to the provider.
From this pact a mocked/stubbed provider can be definied on the consumer side and a mocked/stubbed consumer can be defined on the provider side which makess it possible to ensure the integration for both sides of the contract.

This is an early prototype of a lightweight adaption of pact-jvm7.0 for the Android-vm. It is only meant for the consumer side of pact,
therefore it only contains pact creation, serialization and consumer matching. This adaption was mostly created to get rid of the spring framework and additonal unused HTTPClients to maintain a small library on android so that you can use the HttpLibrary of your choice.
Also, this lightweight adaption requires neither groovy nor ruby anymore.
The last section describes how to integrate pacts with okhttp.

### Pact creation
Just like the normal pact-jvm project, the PactDSL can be used in this implementation.
As of now, regular expressions in the DSL are untested and are most likely not working, since the MatchingRules have no use in the consumer-matching right now.

### Serialization
A single object au.com.dius.pact.external.PactJsonifier has been added which allows easy serialization of a list of pacts by merging them first and writing them into a file afterwards.

### Consumer matching
