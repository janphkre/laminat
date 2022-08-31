package com.janphkre.laminat.retrofit.annotations.header

/**
 * Match a header with the specified key with a regex.
 * Needs to be specified in a {@see MatchHeaders} annotation or
 * on a value parameter of a retrofit method that is also
 * annotated with a {@see retrofit2.http.Header} annotation.
 * The key of that Header annotation is used in that case.
 * @property key The key (path) of the header.
 * @property regex The regex to match with the value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchHeader(
    val key: String = "",
    val regex: String
)
