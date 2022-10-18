package de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment

import de.tum.informatics.www1.artemis.native_app.android.util.parceler.InstantParceler
import kotlinx.datetime.Instant
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@SerialName("FILE")
data class AttachmentFile(
    override val id: Int,
    override val name: String,
    override val visibleToStudents: Boolean,
    val link: String,
    val version: Int,
    val uploadDate: @WriteWith<InstantParceler> Instant? = null,
    val releaseDate: @WriteWith<InstantParceler> Instant? = null,
) : Attachment()