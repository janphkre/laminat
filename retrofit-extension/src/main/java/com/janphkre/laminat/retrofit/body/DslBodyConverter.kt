package com.janphkre.laminat.retrofit.body

import au.com.dius.pact.consumer.dsl.DslPart
import okio.Buffer

interface DslBodyConverter {
    fun toPactDsl(retrofitBody: Buffer, bodyMatches: BodyMatchElement?): DslPart
}