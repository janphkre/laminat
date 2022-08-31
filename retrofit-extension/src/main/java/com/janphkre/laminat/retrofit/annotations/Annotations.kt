package com.janphkre.laminat.retrofit.annotations

import au.com.dius.pact.external.PactBuildException
import com.janphkre.laminat.retrofit.annotations.body.IMatchArray
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMaxArray
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMaxArrays
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMinArray
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyMinArrays
import com.janphkre.laminat.retrofit.annotations.body.MatchBodyRegexes
import com.janphkre.laminat.retrofit.annotations.body.MatchRegex
import com.janphkre.laminat.retrofit.annotations.body.MaxArray
import com.janphkre.laminat.retrofit.annotations.body.MinArray
import com.janphkre.laminat.retrofit.annotations.header.ExcludeHeaders
import com.janphkre.laminat.retrofit.annotations.header.MatchHeader
import com.janphkre.laminat.retrofit.annotations.header.MatchHeaders
import com.janphkre.laminat.retrofit.annotations.query.MatchQuery
import com.janphkre.laminat.retrofit.annotations.query.MatchQuerys
import java.lang.reflect.Method
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.Query

class Annotations(
    private val retrofitMethod: Method
) {

    val excludeHeaders = getRegexes<ExcludeHeaders, ExcludeHeaders, Header, Unit>(
        { values.map { Pair(it, Unit) } },
        { Pair("", Unit) },
        { value }
    )
    val headerRegexes = getRegexes<MatchHeaders, MatchHeader, Header, String>(
        { values.map { Pair(it.key, it.regex) } },
        { Pair(key, regex) },
        { value }
    )
    val queryRegexes = getRegexes<MatchQuerys, MatchQuery, Query, String>(
        { values.map { Pair(it.key, it.regex) } },
        { Pair(key, regex) },
        { value }
    )
    val bodyRegexes = getRegexes<MatchBodyRegexes, MatchRegex, Field, String>(
        { values.map { Pair(it.key, it.regex) } },
        { Pair(key, regex) },
        { "$.$value" }
    )
    val bodyArrays = getRegexes<MatchBodyMinArrays, MatchBodyMinArray, Field, IMatchArray>(
        { values.map { Pair(it.path, MinArray(it.minCount)) } },
        { Pair(path, MinArray(minCount)) },
        { "$.$value" }
    ).plus(
        getRegexes<MatchBodyMaxArrays, MatchBodyMaxArray, Field, IMatchArray>(
            { values.map { Pair(it.path, MaxArray(it.maxCount)) } },
            { Pair(path, MaxArray(maxCount)) },
            { "$.$value" }
        )
    )
    val pathRegex = getMethodPathRegex()

    /**
     * Grabs all regexes from the given annotation type T
     * and converts them through transformAnnotation into pairs of strings.
     * For all regexes there must be a key specified to which the regex belongs.
     * For annotations specified on parameters it checks the type U as a key
     * if transformAnnotation returns an empty key for the annotation of type T.
     *
     * @return a map of all given regexes in the annotations.
     */
    private inline fun <reified T, reified U, reified V, W> getRegexes(
        transformMethodTarget: T.() -> List<Pair<String, W>>,
        transformParameterTarget: U.() -> Pair<String, W>,
        transformReplacement: V.() -> String
    ): Map<String, W> {
        val methodRegexes = getMethodRegexes(transformMethodTarget)
        val parameterRegexes = getMethodParameterRegexes(
            transformParameterTarget,
            transformReplacement
        )
        val resultMap = HashMap<String, W>(
            methodRegexes.size + parameterRegexes.size
        ).apply {
            putAll(methodRegexes)
            putAll(parameterRegexes)
        }
        if (resultMap.size != (methodRegexes.size + parameterRegexes.size)) {
            raiseException("Multiple @${T::class.java.simpleName} with the same key were specified")
        }
        return resultMap
    }

    private inline fun <reified T, W> getMethodRegexes(
        transformTarget: T.() -> List<Pair<String, W>>
    ): List<Pair<String, W>> {
        val methodRegexes = retrofitMethod.annotations
            .filterIsInstance<T>()
            .flatMap(transformTarget)
        methodRegexes.forEach {
            if (it.first.isBlank()) {
                raiseException("@${T::class.java.simpleName} requires a key if specified on the method directly") // ktlint-disable max-line-length
            }
        }
        return methodRegexes
    }

    private inline fun <reified U, reified V, W> getMethodParameterRegexes(
        transformParameterTarget: U.() -> Pair<String, W>,
        transformReplacement: V.() -> String
    ): List<Pair<String, W>> {
        return retrofitMethod.parameterAnnotations.flatMap { parameterAnnotations ->
            val replacementKey = parameterAnnotations
                .filterIsInstance(V::class.java)
                .firstOrNull()
                ?.transformReplacement()
            parameterAnnotations.filterIsInstance(U::class.java).map { targetAnnotation ->
                val targetRegex = targetAnnotation.transformParameterTarget()
                when {
                    targetRegex.first.isNotBlank() -> targetRegex
                    replacementKey != null -> Pair(replacementKey, targetRegex.second)
                    else -> raiseException("@${U::class.java.simpleName} is specified on a parameter without an header key") // ktlint-disable max-line-length
                }
            }
        }
    }

    private fun getMethodPathRegex(): String? {
        val foundAnnotations = retrofitMethod.annotations.filterIsInstance(MatchPath::class.java)
        if (foundAnnotations.size > 1) {
            raiseException("Multiple @MatchPath specified")
        }
        return foundAnnotations.firstOrNull()?.regex
    }

    private fun raiseException(message: String, cause: Exception? = null): Nothing {
        throw PactBuildException("$message on $retrofitMethod", cause)
    }
}
