package com.janphkre.laminat.retrofit.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchPath(
    val regex: String
)