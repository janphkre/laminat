package com.janphkre.laminat.retrofit.annotations.header

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class MatchHeader(
    val key: String = "",
    val regex: String
)