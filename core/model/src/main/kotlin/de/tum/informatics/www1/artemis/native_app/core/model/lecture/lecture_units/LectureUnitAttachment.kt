package de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("attachment")
data class LectureUnitAttachment(
    override val id: Long = 0,
    override val name: String? = null,
    override val releaseDate: Instant? = null,
    override val visibleToStudents: Boolean = true,
    override val completed: Boolean = false,
    val description: String? = null,
    val attachment: Attachment? = null
) : LectureUnit() {
}