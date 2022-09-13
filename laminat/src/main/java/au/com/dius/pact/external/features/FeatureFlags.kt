package au.com.dius.pact.external.features

import java.util.EnumMap

object FeatureFlags {

    private val features: MutableMap<Feature, Boolean> = EnumMap(Feature::class.java)

    fun enableFeature(feature: Feature) {
        features[feature] = true
    }

    fun disableFeature(feature: Feature) {
        features[feature] = false
    }

    fun restoreDefault(feature: Feature) {
        features[feature] = feature.default
    }

    fun isFeatureEnabled(feature: Feature): Boolean {
        return features[feature] ?: feature.default
    }
}