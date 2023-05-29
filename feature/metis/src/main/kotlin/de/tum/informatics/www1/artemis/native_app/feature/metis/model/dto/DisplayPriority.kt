package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import kotlinx.serialization.Serializable

@Serializable
enum class DisplayPriority {
    PINNED,
    ARCHIVED,
    NONE
}