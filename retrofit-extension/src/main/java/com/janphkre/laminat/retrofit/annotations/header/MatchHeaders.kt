package com.janphkre.laminat.retrofit.annotations.header

/**
 * Define a list of definitions for request headers that should
 * be matched with regexes.
 * @property values the regex header definitions
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchHeaders(
    val values: Array<MatchHeader>
)