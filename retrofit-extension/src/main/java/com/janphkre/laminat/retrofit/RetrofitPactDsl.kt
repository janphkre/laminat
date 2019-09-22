package com.janphkre.laminat.retrofit

import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import au.com.dius.pact.external.PactBuildException
import org.apache.http.entity.ContentType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.OPTIONS
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.QueryName
import retrofit2.http.Streaming
import retrofit2.http.Tag
import retrofit2.http.Url
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RetrofitPactDsl<T>(
    private val pactDslRequestWithoutPath: PactDslRequestWithoutPath,
    private val retrofitMethod: KFunction<T>,
    private val retrofit: Retrofit
) {

    //Method may only be null during the init because processTopLevelAnnotations throws
    private var method: String? = null
    //Path may only be null during the init because processTopLevelAnnotations throws
    private var path: String? = null
    private val headers: HashMap<String, String> = HashMap()
    private var isMultipart = false
    private var isFormEncoded = false

    init {
        processTopLevelAnnotations()
        //TODO: What is the default method for retrofit?
        //TODO: WHAT IS THE DEFAULT CONTENT-TYPE FOR RETROFIT?
        //TODO: ENFORCE THE CONTENT-TYPE SET BY RETROFIT ON THE PACT
        if(method == null) {
            raiseException("Could not find any retrofit method annotations")
        }
    }

    fun withParameters(vararg parameterValues: Any?): RetrofitPactDsl<T> {
        if(retrofitMethod.parameters.size != parameterValues.size) {
            raiseException("Expected ${retrofitMethod.parameters.size} parameters but got ${parameterValues.size} parameters")
        }
        retrofitMethod.parameters.forEachIndexed { index, parameter ->
            //TODO: ENSURE THAT parameterValues[index] has the same type as parameter
            processParameterAnnotation(parameter, parameterValues[index], index)
        }
        if(path == null) {
            raiseException("No path was specified in the retrofit method or parameter annotations")
        }
        return this
    }

    fun willRespondWith(responseValue: Any): PactDslRequestWithPath {//TODO: DO WE NEED TO SUPPORT THIS METHOD OR can we just return someting else?
        //TODO: MAYBE CHECK THAT path does not contain non inflated parameters? etc.
        return pactDslRequestWithoutPath.method(method!!)
            .path(path!!)
            .headers(headers)
            .query(TODO())
            .body(TODO())
    }

    private fun processTopLevelAnnotations() {
        retrofitMethod.annotations.forEach { annotation ->
            when(annotation) {
                is GET -> httpVerb("GET", annotation.value)
                is POST -> httpVerb("POST", annotation.value)
                is DELETE -> httpVerb("DELETE", annotation.value)
                is PATCH -> httpVerb("PATCH", annotation.value)
                is PUT -> httpVerb("PUT", annotation.value)
                is HEAD -> httpVerb("HEAD", annotation.value)
                is OPTIONS -> httpVerb("OPTIONS", annotation.value)
                is HTTP -> httpVerb(annotation.method, annotation.path)
                is Headers -> annotation.value.forEach(::processTopLevelHeader)
                is FormUrlEncoded -> {
                    header(ContentType.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.mimeType)
                    isFormEncoded = true
                }
                is Multipart -> {
                    //TODO: MAYBE SET A HEADER?
                    isMultipart = true
                }
                is Streaming -> {
                    TODO("Does this have any relevance for the pact?")
                }
            }
        }
    }

    private fun processTopLevelHeader(header: String) {
        val colonIndex = header.indexOf(':')
        if (colonIndex == -1 || colonIndex == 0 || colonIndex == header.length - 1) {
            raiseException("@Headers value must be in the form \"Name: Value\". Found: \"$header\"")
        }
        val headerName = header.substring(0, colonIndex)
        val headerValue = header.substring(colonIndex + 1).trim { it <= ' ' }
        header(headerName, headerValue)
    }

    private fun processParameterAnnotation(parameter: KParameter, value: Any?, index: Int) {
        parameter.annotations.forEach { annotation ->
            when(annotation) {
                is Body -> processBody()
                is Field -> processField()
                is FieldMap -> processFieldMap()
                is Header -> processHeader(parameter, annotation, value)
                is HeaderMap -> processHeaderMap(parameter, value)
                is Part -> processPart()
                is PartMap -> processPartMap()
                is Path -> processPath(parameter, value)
                is Query -> processQuery()
                is QueryMap -> processQueryMap()
                is QueryName -> processQueryName()
                is Tag -> processTag()
                is Url -> processUrl(value, index)
            }
        }
    }

    private fun processBody() {
        if (isFormEncoded || isMultipart) {
            raiseException("@Body parameters cannot be used with form or multi-part encoding.")
        }
        //TODO: only allow a single body
        TODO("Serialize Body into json, use custom annotations to add regexes")
    }

    private fun processField() {
        if(!isFormEncoded) {
            raiseException("@Field parameter specified but request is not annotated with @FormUrlEncoded")
        }
        TODO()
    }

    private fun processFieldMap() {
        if(!isFormEncoded) {
            raiseException("@FieldMap parameter specified but request is not annotated with @FormUrlEncoded")
        }
        TODO()
    }

    private fun processHeader(parameter: KParameter, annotation: Header, value: Any?) {
        if(value == null) {
            return
        }
        val annotations = parameter.annotations.toTypedArray()
        header(annotation.value,
            (value as? Iterable<*>)
                ?.filterNotNull()
                ?.joinToString(separator = ", ") { convertToString(it, annotations) }
                ?: convertToString(value, annotations)
        )
    }

    private fun processHeaderMap(parameter: KParameter, value: Any?) {
        if(value == null) {
            return
        }
        if(value is okhttp3.Headers) {
            for(valueIndex in 0 until value.size()) {
                header(value.name(valueIndex), value.value(valueIndex))
            }
        } else {
            (value as? Map<*, *>)?.entries?.forEach { headerEntry ->
                if(headerEntry.key !is String) {
                    raiseException("Recieved invalid @HeaderMap type parameter. Header keys must be of type string")
                }
                if(headerEntry.value != null) {
                    header(
                        headerEntry.key as String,
                        convertToString(
                            headerEntry.value!!,
                            parameter.annotations.toTypedArray()
                        )
                    )
                }
            } ?: raiseException("Received invalid @HeaderMap parameter. Must be of type Map or okhttp3.Headers!")
        }
    }

    private fun processPart() {
        if(!isMultipart) {
            raiseException("@Part parameter specified but request is not annotated with @Multipart")
        }
        TODO()
    }

    private fun processPartMap() {
        if(!isMultipart) {
            raiseException("@PartMap parameter specified but request is not annotated with @Multipart")
        }
        TODO()
    }

    private fun processPath(parameter: KParameter, value: Any?) {
        if(value == null) {
            raiseException("Received null for @Path parameter ${parameter.name}")
        }
        path!!.replace(
            "{${parameter.name}}",
            convertToString(value, parameter.annotations.toTypedArray())
        )
    }

    private fun processQuery() {
        TODO()
    }

    private fun processQueryMap() {
        TODO()
    }

    private fun processQueryName() {
        TODO()
    }

    private fun processTag() {
        TODO("WHAT IS A TAG?")
    }

    private fun processUrl(value: Any?, index: Int) {
        if(index != 0) {
            raiseException("@Url parameter must be specified as the first parameter")
        }
        path = (value as? String) ?: raiseException("@Url parameter must be a not null String")
    }

    private fun <T: Any> convertToString(value: T, annotations: Array<Annotation>): String {
        return retrofit.stringConverter<T>(value::class.java, annotations).convert(value) ?: value.toString()
    }

    private fun httpVerb(method: String, path: String?) {
        if(this.method != null) {
            raiseException("Duplicate method annotation specified ${this.method} & $method")
        }
        this.method = method

        path(path)
    }

    private fun path(path: String?) {
        if(path == null) {
            return
        }
        if(this.path != null) {
            raiseException("Duplicate path specified ${this.path} & $path")
        }
        this.path = path
    }

    private fun header(headerName: String, headerValue: String) {
        val normalizedHeaderName = if (ContentType.CONTENT_TYPE.equals(headerName, ignoreCase = true)) {
            ContentType.CONTENT_TYPE
        } else {
            headerName
        }
        val existingHeader = headers[normalizedHeaderName]?.plus(", ") ?: ""
        headers[normalizedHeaderName] = existingHeader.plus(headerValue)
    }

    private fun raiseException(cause: String): Nothing {
        throw PactBuildException("$cause in $retrofitMethod")
    }

    fun toPact(): PactDslRequestWithPath {
        TODO("Convert Retrofit Pact to PactDslRequestWithPath")
    }
}
