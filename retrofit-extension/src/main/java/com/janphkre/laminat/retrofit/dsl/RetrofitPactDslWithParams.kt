package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.consumer.dsl.PactDslResponse
import com.janphkre.laminat.retrofit.annotations.Annotations
import com.janphkre.laminat.retrofit.body.BodyMatchElement
import com.janphkre.laminat.retrofit.body.RetrofitPactDslBodyCreator
import java.lang.reflect.Method
import okhttp3.RetrofitPactRequestWithParams

class RetrofitPactDslWithParams(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    retrofitMethod: Method,
    private val retrofitRequest: RetrofitPactRequestWithParams
) {

    private val anyMatchRegex = ".*"
    private val annotations = Annotations(retrofitMethod)

    /**
     * Converts the retrofit pact dsl back to a pact dsl.
     */
    @Suppress("unused")
    fun toPactDsl(): PactDslRequestWithPath {
        val intermediatePact = pactDslRequestWithoutPath.method(retrofitRequest.method)
            .let { pactDsl ->
                if (annotations.pathRegex == null) {
                    pactDsl.path(retrofitRequest.relativePath)
                } else {
                    pactDsl.matchPath(annotations.pathRegex, retrofitRequest.relativePath)
                }
            }
            .apply {
                retrofitRequest.headers.forEach { header ->
                    if (annotations.excludeHeaders[header.first] != null) {
                        return@forEach
                    }
                    val regex = annotations.headerRegexes[header.first]
                    if (regex == null) {
                        headers(header.first, header.second)
                    } else {
                        matchHeader(header.first, regex, header.second)
                    }
                }
                retrofitRequest.query.forEach { parameter ->
                    val regex = annotations.queryRegexes[parameter.first] ?: anyMatchRegex
                    matchQuery(parameter.first, regex, parameter.second)
                }
            }
        // TODO not accounting for MultiPart at the moment?
        if (retrofitRequest.body?.contentType() == null) {
            return intermediatePact
        }
        val dslBody = RetrofitPactDslBodyCreator(
            retrofitRequest.body,
            BodyMatchElement.from(annotations.bodyRegexes, annotations.bodyArrays)
        ).create()
        return intermediatePact.apply {
            body(dslBody)
        }
    }

    @Suppress("unused")
    fun body(dslPart: DslPart): PactDslRequestWithPath {
        return toPactDsl().body(dslPart)
    }

    @Suppress("unused")
    fun willRespondWith(): PactDslResponse {
        return toPactDsl().willRespondWith()
    }
}
