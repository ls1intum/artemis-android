package de.tum.informatics.www1.artemis.native_app.core.model.metis

import kotlinx.serialization.Serializable

@Serializable
enum class DisplayPriority {
    PINNED,
    ARCHIVED,
    NONE
}