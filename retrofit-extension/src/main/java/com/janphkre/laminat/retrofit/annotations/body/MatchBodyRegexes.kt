package com.janphkre.laminat.retrofit.annotations.body

/**
 * Define a list of definitions where in the structure of the body
 * a string should be matched with a regex instead of expecting a hardcoded string.
 * @property values the regex definitions
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyRegexes(
    val values: Array<MatchRegex>
)