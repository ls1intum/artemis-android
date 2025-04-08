package de.tum.informatics.www1.artemis.native_app.feature.force_update


enum class Feature(val rawValue: String) {
    CourseNotifications("CourseSpecificNotifications");
}

object FeatureAvailability {

    private var availableFeatures: List<String> = emptyList()

    fun setAvailableFeatures(features: List<String>) {
        availableFeatures = features
    }

    fun isEnabled(feature: Feature): Boolean {
        return availableFeatures.contains(feature.rawValue)
    }
}
