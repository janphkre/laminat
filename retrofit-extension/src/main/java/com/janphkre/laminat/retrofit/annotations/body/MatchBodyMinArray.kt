package com.janphkre.laminat.retrofit.annotations.body

/**
 * Match an array at the given path of the body to not contain less than minCount elements.
 * Child element behavior can be specified as a [*] path.
 * @property path The path through the object tree where the array resides.
 * Root is specified as $
 * @property minCount The minimum count of elements that the array should hold.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyMinArray(
    val path: String = "$",
    val minCount: Int
)
