package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.data.service.EnumOrdinalSerializer
import kotlinx.serialization.Serializable

@Serializable(with = SavedPostPostingTypeSerializer::class)
enum class SavedPostPostingType {
    POST,
    ANSWER,
}

class SavedPostPostingTypeSerializer : EnumOrdinalSerializer<SavedPostPostingType>(
    serialName = SavedPostPostingType::class.simpleName!!,
    values = SavedPostPostingType.entries.toTypedArray()
)