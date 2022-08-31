package com.janphkre.laminat.retrofit.annotations.body

/**
 * Match an array at the given path of the body to not contain more than maxCount elements.
 * Child element behavior can be specified as a [*] path.
 * @property path The path through the object tree where the array resides.
 * Root is specified as $
 * @property maxCount The maximum count of elements that the array should hold.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyMaxArray(
    val path: String = "$",
    val maxCount: Int
)
