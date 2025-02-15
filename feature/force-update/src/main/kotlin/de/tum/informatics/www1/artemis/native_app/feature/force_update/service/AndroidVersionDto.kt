package de.tum.informatics.www1.artemis.native_app.feature.force_update.service

import kotlinx.serialization.Serializable

@Serializable
data class AndroidVersionDto(
    val min: String,
    val recommended: String
)
