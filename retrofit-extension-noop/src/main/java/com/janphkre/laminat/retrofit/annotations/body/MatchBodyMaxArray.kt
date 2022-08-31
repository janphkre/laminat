package com.janphkre.laminat.retrofit.annotations.body

@Retention(AnnotationRetention.SOURCE)
annotation class MatchBodyMaxArray(
    val path: String = "$",
    val maxCount: Int
)