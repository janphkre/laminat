package com.janphkre.laminat.retrofit.annotations.query

/**
 * Define a list of definitions for request query parameters
 * that should be matched with regexes.
 * @property values the regex query definitions
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchQuerys(
    val values: Array<MatchQuery>
)