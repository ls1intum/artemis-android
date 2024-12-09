package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model

import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    val path: String?,
    val status: Int? = null
)