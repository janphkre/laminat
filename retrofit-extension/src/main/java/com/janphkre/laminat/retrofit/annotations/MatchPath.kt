package com.janphkre.laminat.retrofit.annotations

/**
 * Define a regex for the path of the request.
 * @property regex the regex definition
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchPath(
    val regex: String
)