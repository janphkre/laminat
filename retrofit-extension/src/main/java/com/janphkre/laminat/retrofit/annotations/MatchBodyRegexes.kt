package com.janphkre.laminat.retrofit.annotations

import com.janphkre.laminat.retrofit.annotations.data.MatchRegex

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyRegexes(
    val values: Array<MatchRegex>
)