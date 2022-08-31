package com.janphkre.laminat.retrofit.annotations.body

/**
 * Match a string at a specified key in the body with a regex.
 * When this annotation is specified on a value paramter of a retrofit method
 * that is also annotated with a {@see retrofit2.http.Field} annotation, the key of that
 * field annotation is used.
 * @property key The key (path) to the string value in the body.
 * @property regex The regex to match with the value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchRegex(
    val key: String = "",
    val regex: String
)
