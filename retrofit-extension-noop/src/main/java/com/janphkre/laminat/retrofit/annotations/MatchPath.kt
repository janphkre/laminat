package com.janphkre.laminat.retrofit.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class MatchPath(
    val regex: String
)