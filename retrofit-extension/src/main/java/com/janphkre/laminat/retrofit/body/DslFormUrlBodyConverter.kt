package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import okio.Buffer

object DslFormUrlBodyConverter : DslBodyConverter {

    override fun toPactDsl(retrofitBody: Buffer, bodyMatches: BodyMatchElement?): DslPart {
        TODO("not implemented")
    }
}