package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslRootValue
import okhttp3.RequestBody
import okio.Buffer
import org.apache.http.entity.ContentType

class RetrofitPactDslBodyCreator(
    private val retrofitBody: RequestBody,
    private val bodyMatches: BodyMatchElement?
) {

    fun create(): DslPart {
        if (retrofitBody.contentLength() == 0L) {
            return PactDslRootValue().apply { setValue("") }
        }
        val contentTypeString = retrofitBody.contentType()?.let { "${it.type}/${it.subtype}" }
        return Buffer().use { retrofitBodyBuffer ->
            retrofitBody.writeTo(retrofitBodyBuffer)
            val dslBodyConverter: DslBodyConverter =
                dslBodies[contentTypeString] ?: DslPlainTextBodyConverter
            dslBodyConverter.toPactDsl(retrofitBodyBuffer, bodyMatches)
        }
    }

    companion object {
        private val dslBodies = HashMap<String, DslBodyConverter>(4).apply {
            this[ContentType.APPLICATION_JSON.mimeType] = DslJsonBodyConverter
            this[ContentType.APPLICATION_JSONREQUEST.mimeType] = DslJsonBodyConverter
            this[ContentType.APPLICATION_JSON_RPC.mimeType] = DslJsonBodyConverter
            this[ContentType.APPLICATION_FORM_URLENCODED.mimeType] = DslPlainTextBodyConverter
            this[ContentType.TEXT_PLAIN.mimeType] = DslPlainTextBodyConverter
        }

        /**
         * Allows to declare a DslBodyConverter for a given mime type that will be used
         * when creating the pact from the retrofit instance.
         */
        fun setBodyConverter(mimeType: String, converter: DslBodyConverter) {
            dslBodies[mimeType] = converter
        }
    }
}
