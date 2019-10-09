package com.janphkre.laminat.retrofit.annotations

import com.janphkre.laminat.retrofit.annotations.data.MinArray

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatchBodyMinArrays(
    val values: Array<MinArray>
)