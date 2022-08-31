package com.janphkre.laminat.retrofit.annotations.header

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ExcludeHeaders (
    val values: Array<String> = []
)