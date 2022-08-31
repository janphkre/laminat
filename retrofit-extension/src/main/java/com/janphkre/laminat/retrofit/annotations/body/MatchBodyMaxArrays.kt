package com.janphkre.laminat.retrofit.annotations.body

/**
 * Define a list of definitions where in the structure of the body
 * an array should contain generic elements and be limited in it's
 * size.
 * @property values the maximum array definitions
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyMaxArrays(
    val values: Array<MatchBodyMaxArray>
)
