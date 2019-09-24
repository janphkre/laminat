package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.external.PactBuildException
import okhttp3.RequestBody
import okio.Buffer
import org.apache.http.entity.ContentType
import java.lang.reflect.Method

class RetrofitPactDslBodyCreator(
    private val retrofitMethod: Method,
    private val retrofitBody: RequestBody,
    private val bodyMatches: BodyMatchElement?
) {

    fun create(): DslPart {
        val contentType = retrofitBody.contentType() ?: throw PactBuildException("No content type specified on request body in $retrofitMethod")
        val contentTypeString = "${contentType.type()}/${contentType.subtype()}"
        return Buffer().use { retrofitBodyBuffer ->
            retrofitBody.writeTo(retrofitBodyBuffer)
            val dslBodyConverter: DslBodyConverter = dslBodies[contentTypeString] ?: DslPlainTextBodyConverter
            dslBodyConverter.toPactDsl(retrofitBodyBuffer, bodyMatches)
        }

    }

    companion object {
        private val dslBodies = HashMap<String, DslBodyConverter>(4).apply {
            this[ContentType.APPLICATION_JSON.mimeType] = DslJsonBodyConverter
            this[ContentType.APPLICATION_JSONREQUEST.mimeType] = DslJsonBodyConverter
            this[ContentType.APPLICATION_JSON_RPC.mimeType] = DslJsonBodyConverter
            this[ContentType.TEXT_PLAIN.mimeType] = DslPlainTextBodyConverter
        }

        fun setBodyConverter(mimeType: String, converter: DslBodyConverter) {
            dslBodies[mimeType] = converter
        }
    }
}