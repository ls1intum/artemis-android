package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val id: Int = 0,
    val name: String? = null,
    val visibleToStudents: Boolean = true,
    val link: String? = null,
    val releaseDate: Instant? = null,
    val version: Int? = null,
    val uploadDate: Instant? = null,
    val attachmentType: AttachmentType
) {
    enum class AttachmentType {
        FILE,
        URL
    }
}