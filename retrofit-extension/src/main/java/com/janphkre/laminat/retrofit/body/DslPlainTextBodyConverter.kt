package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.external.PactBuildException
import okio.Buffer

object DslPlainTextBodyConverter: DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer, bodyMatches: BodyMatchElement?): DslPart {
        if(bodyMatches != null && bodyMatches !is BodyMatchElement.BodyMatchString) {
            throw PactBuildException("Partial pact regex matches are unsupported with a plain text matcher $bodyMatches")
        }
        TODO("not implemented")
    }
}