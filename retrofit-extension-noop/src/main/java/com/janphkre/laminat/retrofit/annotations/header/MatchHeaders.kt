package com.janphkre.laminat.retrofit.annotations.header

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class MatchHeaders(
    val values: Array<MatchHeader>
)