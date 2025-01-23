package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.data.service.EnumOrdinalSerializer
import kotlinx.serialization.Serializable

@Serializable(with = SavedPostStatusSerializer::class)
enum class SavedPostStatus {
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}

class SavedPostStatusSerializer : EnumOrdinalSerializer<SavedPostStatus>(
    serialName = SavedPostStatus::class.simpleName!!,
    values = SavedPostStatus.entries.toTypedArray()
)