package com.janphkre.laminat.retrofit.annotations.header

/**
 * Specify a list of headers that should be excluded from the pact.
 * This can be used either with a list of header names that should be excluded,
 * or be specified on a value parameter that is also annotated with {@see retrofit2.http.Header}.
 * In the second case, the value of the Header annotation is used as the key for this filter.
 * @property values The list of header keys that should be excluded from the pact.
 * May be empty on value parameters.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeHeaders(
    val values: Array<String> = []
)
