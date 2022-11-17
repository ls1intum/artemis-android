package de.tum.informatics.www1.artemis.native_app.android.model.lecture.attachment

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("FILE")
data class AttachmentFile(
    override val id: Int,
    override val name: String,
    override val visibleToStudents: Boolean,
    val link: String,
    val version: Int,
    val uploadDate: Instant? = null,
    val releaseDate: Instant? = null,
) : Attachment()