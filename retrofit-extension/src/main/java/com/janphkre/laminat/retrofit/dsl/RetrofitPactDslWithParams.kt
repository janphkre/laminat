package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.consumer.dsl.PactDslResponse
import au.com.dius.pact.external.PactBuildException
import com.janphkre.laminat.retrofit.annotations.MatchBodyMaxArrays
import com.janphkre.laminat.retrofit.annotations.MatchBodyMinArrays
import com.janphkre.laminat.retrofit.annotations.MatchBodyRegexes
import com.janphkre.laminat.retrofit.annotations.MatchHeaders
import com.janphkre.laminat.retrofit.annotations.MatchPath
import com.janphkre.laminat.retrofit.annotations.MatchQuerys
import com.janphkre.laminat.retrofit.body.BodyMatchElement
import com.janphkre.laminat.retrofit.body.RetrofitPactDslBodyCreator
import okhttp3.RetrofitPactRequestWithParams
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.Query
import java.lang.reflect.Method

class RetrofitPactDslWithParams(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofitMethod: Method,
    private val retrofitRequest: RetrofitPactRequestWithParams
) {

    private val anyMatchRegex = ".*"
    private val headerRegexes = getRegexes<MatchHeaders, Header, String>({ values.map { Pair(it.key, it.regex) } }, { value })
    private val queryRegexes = getRegexes<MatchQuerys, Query, String>({ values.map { Pair(it.key, it.regex) } }, { value })
    private val bodyRegexes = getRegexes<MatchBodyRegexes, Field, String>({ values.map { Pair(it.key, it.regex) } }, { "$.$value" })
    private val bodyArrays = getRegexes<MatchBodyMinArrays, Field, Int>({ values.map { Pair(it.path, -it.minCount) } }, { "$.$value" })
        .plus(getRegexes<MatchBodyMaxArrays, Field, Int>({ values.map { Pair(it.path, it.maxCount) } }, { "$.$value" }))
    private val pathRegex = getPathRegex()

    /**
     * Grabs all regexes from the given annotation type T
     * and converts them through transformAnnotation into pairs of strings.
     * For all regexes there must be a key specified to which the regex belongs.
     * For annotations specified on parameters it checks the type U as a key
     * if transformAnnotation returns an empty key for the annotation of type T.
     *
     * @return a map of all given regexes in the annotations.
     */
    private inline fun <reified T, reified U, V> getRegexes(
        transformTarget: T.() -> List<Pair<String, V>>,
        transformReplacement: U.() -> String
    ): Map<String, V> {
        val methodRegexes = retrofitMethod.annotations.filterIsInstance(T::class.java).flatMap(transformTarget)
        methodRegexes.forEach {
            if (it.first.isBlank()) {
                raiseException("@${T::class.java.simpleName} requires a key if specified on the method directly")
            }
        }
        val parameterRegexes = retrofitMethod.parameterAnnotations.flatMap { parameterAnnotations ->
            val replacementKey = parameterAnnotations.filterIsInstance(U::class.java).firstOrNull()?.transformReplacement()
            parameterAnnotations.filterIsInstance(T::class.java).flatMap { targetAnnotation ->
                targetAnnotation.transformTarget().map { targetRegex ->
                    when {
                        targetRegex.first.isNotBlank() -> targetRegex
                        replacementKey != null -> Pair(replacementKey, targetRegex.second)
                        else -> raiseException("@${T::class.java.simpleName} is specified on a parameter without an header key")
                    }
                }
            }
        }
        val resultMap = HashMap<String, V>(methodRegexes.size + parameterRegexes.size).apply {
            putAll(methodRegexes)
            putAll(parameterRegexes)
        }
        if (resultMap.size != (methodRegexes.size + parameterRegexes.size)) {
            raiseException("Multiple @${T::class.java.simpleName} with the same key were specified")
        }
        return resultMap
    }

    private fun getPathRegex(): String? {
        val foundAnnotations = retrofitMethod.annotations.filterIsInstance(MatchPath::class.java)
        if (foundAnnotations.size > 1) {
            raiseException("Multiple @MatchPath specified")
        }
        return foundAnnotations.firstOrNull()?.regex
    }

    private fun completeParameters(): PactDslRequestWithPath {
        val intermediatePact = pactDslRequestWithoutPath.method(retrofitRequest.method)
            .let { pactDsl ->
                if (pathRegex == null) {
                    pactDsl.path(retrofitRequest.relativePath)
                } else {
                    pactDsl.matchPath(pathRegex, retrofitRequest.relativePath)
                }
            }
            .apply {
                retrofitRequest.headers.forEach { header ->
                    val regex = headerRegexes[header.first]
                    if (regex == null) {
                        headers(header.first, header.second)
                    } else {
                        matchHeader(header.first, regex, header.second)
                    }
                }
                retrofitRequest.query.forEach { parameter ->
                    val regex = queryRegexes[parameter.first] ?: anyMatchRegex
                    matchQuery(parameter.first, regex, parameter.second)
                }
            }
        //TODO not accounting for MultiPart or FormUrlEncoded at the moment?
        if (retrofitRequest.body == null) {
            return intermediatePact
        }
        val dslBody = RetrofitPactDslBodyCreator(
            retrofitMethod,
            retrofitRequest.body,
            BodyMatchElement.from(bodyRegexes, bodyArrays)
        ).create()
        return intermediatePact.apply {
            if (dslBody != null) {
                body(dslBody)
            }
        }
    }

    fun body(dslPart: DslPart): PactDslRequestWithPath {
        return completeParameters().body(dslPart)
    }

    /**
     * Converts the retrofit pact dsl back to a pact dsl.
     * matchPath is unsupported at the moment.
     */
    fun willRespondWith(): PactDslResponse {
        return completeParameters().willRespondWith()
    }

    private fun raiseException(message: String, cause: Exception? = null): Nothing {
        throw PactBuildException("$message on $retrofitMethod", cause)
    }
}