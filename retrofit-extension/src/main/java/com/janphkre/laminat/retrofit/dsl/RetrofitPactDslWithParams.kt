package com.janphkre.laminat.retrofit.dsl

import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.external.PactBuildException
import com.janphkre.laminat.retrofit.annotations.MatchBody
import com.janphkre.laminat.retrofit.annotations.MatchHeader
import com.janphkre.laminat.retrofit.annotations.MatchPath
import com.janphkre.laminat.retrofit.annotations.MatchQuery
import com.janphkre.laminat.retrofit.body.BodyMatchElement
import com.janphkre.laminat.retrofit.body.RetrofitPactDslBodyCreator
import okhttp3.RetrofitPactRequestWithParams
import retrofit2.http.Body
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
    private val headerRegexes = getRegexes<MatchHeader, Header>({ Pair(key, regex) }, { value })
    private val queryRegexes = getRegexes<MatchQuery, Query>({ Pair(key, regex) }, { value })
    private val bodyRegexes = getRegexes<MatchBody, Field>({ Pair(path, regex) }, { "$.value" })
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
    private inline fun <reified T, reified U> getRegexes(
        transformTarget: T.() -> Pair<String, String>,
        transformReplacement: U.() -> String
    ): Map<String, String> {
        val methodRegexes = retrofitMethod.annotations.filterIsInstance(T::class.java).map(transformTarget)
        methodRegexes.forEach {
            if (it.first.isBlank()) {
                raiseException("@${T::class.java.simpleName} requires a key if specified on the method directly")
            }
        }
        val parameterRegexes = retrofitMethod.parameterAnnotations.flatMap { parameterAnnotations ->
            val replacementKey = parameterAnnotations.filterIsInstance(U::class.java).firstOrNull()?.transformReplacement()
            parameterAnnotations.filterIsInstance(T::class.java).map { targetAnnotation ->
                val targetRegex = targetAnnotation.transformTarget()
                when {
                    targetRegex.first.isNotBlank() -> targetRegex
                    replacementKey != null -> Pair(replacementKey, targetRegex.second)
                    else -> raiseException("@${T::class.java.simpleName} is specified on a parameter without an header key")
                }
            }
        }
        val resultMap = HashMap<String, String>(methodRegexes.size + parameterRegexes.size).apply {
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

    private fun getBodyParameterIndex(): Int {
        return retrofitMethod.parameterAnnotations.indexOfFirst { annotations -> annotations.any { it is Body } }
    }

    /**
     * Converts the retrofit pact dsl back to a pact dsl.
     * matchPath is unsupported at the moment.
     */
    fun willRespondWith(): PactDslRequestWithPath {
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
            BodyMatchElement.from(bodyRegexes)
        ).create()
        return intermediatePact.body(dslBody)

    }

    private fun raiseException(message: String, cause: Exception? = null): Nothing {
        throw PactBuildException("$message on $retrofitMethod", cause)
    }
}