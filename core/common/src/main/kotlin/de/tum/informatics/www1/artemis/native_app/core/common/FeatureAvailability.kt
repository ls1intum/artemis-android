package de.tum.informatics.www1.artemis.native_app.core.common

import android.util.Log


enum class Feature(val rawValue: String) {
    CourseNotifications("CourseSpecificNotifications");
}

enum class ActiveModuleFeature(val rawValue: String) {
    Passkey("passkey"),
}

private const val TAG = "FeatureAvailability"

object FeatureAvailability {

    private var availableFeatures: List<String> = emptyList()
    private var activeModuleFeatures: List<String> = emptyList()

    fun setAvailableFeatures(features: List<String>) {
        Log.d(TAG, "Setting available features: $features")
        availableFeatures = features
    }

    fun setActiveModuleFeatures(features: List<String>) {
        Log.d(TAG, "Setting active module features: $features")
        activeModuleFeatures = features
    }

    fun isEnabled(feature: Feature): Boolean {
        return availableFeatures.contains(feature.rawValue)
    }

    fun isEnabled(activeModuleFeature: ActiveModuleFeature): Boolean {
        return activeModuleFeatures.contains(activeModuleFeature.rawValue)
    }
}
