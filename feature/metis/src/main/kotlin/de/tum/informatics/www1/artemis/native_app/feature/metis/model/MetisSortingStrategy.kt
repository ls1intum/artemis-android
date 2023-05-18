package de.tum.informatics.www1.artemis.native_app.feature.metis.model

enum class MetisSortingStrategy(val httpParamValue: String) {
    DATE_ASCENDING("ASCENDING"),
    DATE_DESCENDING("DESCENDING"),
    REPLIES_ASCENDING("ASCENDING"),
    REPLIES_DESCENDING("DESCENDING"),
    VOTES_ASCENDING("ASCENDING"),
    VOTES_DESCENDING("DESCENDING")
}