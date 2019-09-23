package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import okio.Buffer

object DslPlainTextBodyConverter: DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer): DslPart {
        TODO("not implemented")
    }
}