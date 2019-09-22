package com.janphkre.laminat.retrofit.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchQuery(
    val key: String = "",
    val regex: String
)