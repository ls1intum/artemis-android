package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import kotlinx.serialization.Serializable

@Serializable
enum class SavedPostStatus {
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}