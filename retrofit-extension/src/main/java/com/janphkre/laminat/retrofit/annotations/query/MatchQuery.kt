package com.janphkre.laminat.retrofit.annotations.query

/**
 * Match a query with the specified key with a regex.
 * Needs to be specified in a {@see MatchQuerys} annotation or
 * on a value parameter of a retrofit method that is also
 * annotated with a {@see retrofit2.http.Query} annotation.
 * The key of that Query annotation is used in that case.
 * @property key The key (path) of the query.
 * @property regex The regex to match with the value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchQuery(
    val key: String = "",
    val regex: String
)