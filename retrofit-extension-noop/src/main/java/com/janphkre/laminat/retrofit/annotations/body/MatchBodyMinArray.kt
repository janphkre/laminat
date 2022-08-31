package com.janphkre.laminat.retrofit.annotations.body

@Retention(AnnotationRetention.SOURCE)
annotation class MatchBodyMinArray(
    val path: String = "$",
    val minCount: Int
)