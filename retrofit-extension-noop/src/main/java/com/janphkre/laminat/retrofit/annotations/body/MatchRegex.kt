package com.janphkre.laminat.retrofit.annotations.body

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class MatchRegex(
    val key: String = "",
    val regex: String
)