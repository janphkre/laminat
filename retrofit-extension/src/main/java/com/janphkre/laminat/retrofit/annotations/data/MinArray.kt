package com.janphkre.laminat.retrofit.annotations.data

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinArray(
    val path: String = "$",
    val minCount: Int
)