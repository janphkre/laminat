package com.janphkre.laminat.retrofit.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBody(
    val path: String = "",
    val regex: String
)