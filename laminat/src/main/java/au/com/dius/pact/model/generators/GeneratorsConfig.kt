package au.com.dius.pact.model.generators

import org.apache.http.entity.ContentType

object GeneratorsConfig {

    private val contentTypeHandlers: MutableMap<String, ContentTypeHandler> = mutableMapOf()

    init {
        setDefaultContentTypeHandlers()
    }

    fun lookupContentTypeHandler(contentType: String): ContentTypeHandler? {
        return contentTypeHandlers[contentType]
    }

    fun clearContentTypeHandlers() {
        contentTypeHandlers.clear()
    }

    fun setDefaultContentTypeHandlers() {
        contentTypeHandlers[ContentType.APPLICATION_JSON.mimeType] = JsonContentTypeHandler // TODO: XML CONTENT-TYPE HANDLER
        contentTypeHandlers[ContentType.DEFAULT_BINARY.mimeType] = BinaryContentTypeHandler
    }

    fun setContentTypeHandler(contentType: String, handler: ContentTypeHandler) {
        contentTypeHandlers[contentType] = handler
    }
}