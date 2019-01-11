package au.com.dius.pact.model

import java.util.jar.JarInputStream

data class BasePact(val consumer: Consumer, val provider: Provider, val pactSource: PactSource) {

    fun compatibleTo(other: Pact): Boolean {
        return provider == other.provider && this::class.isInstance(other)
    }

    companion object {
        fun lookupVersion(): String  {
            val url = BasePact.protectionDomain?.codeSource?.location
            return if (url != null) {
                val openStream = url.openStream().use {
                    try {
                        def jarStream = new JarInputStream(openStream)
                        jarStream.manifest?.mainAttributes?.getValue('Implementation-Version') ?: ''
                    } catch (e) {
                        log.warn('Could not load pact-jvm manifest', e)
                        ''
                    }
                }
            } else {
                ""
            }
        }

        static Map convertToMap(def object) {
            if (object == null) {
                object
            } else {
                object.properties.findAll { it.key != 'class' }.collectEntries { k, v ->
                    if (v instanceof Map) {
                        [k, convertToMap(v)]
                    } else if (v instanceof Collection) {
                        [k, v.collect { convertToMap(v) } ]
                    } else {
                        [k, v]
                    }
                }
            }
        }
    }
}