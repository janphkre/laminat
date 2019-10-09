package com.janphkre.laminat.retrofit.annotations

import com.janphkre.laminat.retrofit.annotations.data.MaxArray

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyMaxArrays(
    val values: Array<MaxArray>
)