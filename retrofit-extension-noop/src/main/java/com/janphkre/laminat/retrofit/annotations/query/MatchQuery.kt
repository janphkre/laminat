package com.janphkre.laminat.retrofit.annotations.query

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class MatchQuery(
    val key: String = "",
    val regex: String
)