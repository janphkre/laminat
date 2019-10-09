package com.janphkre.laminat.retrofit.annotations.data

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxArray(
    val path: String = "$",
    val maxCount: Int
)