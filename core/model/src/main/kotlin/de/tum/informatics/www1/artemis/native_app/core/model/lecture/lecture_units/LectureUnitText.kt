package de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text")
data class LectureUnitText(
    override val id: Long = 0,
    override val name: String? = null,
    override val releaseDate: Instant? = null,
    override val visibleToStudents: Boolean = true,
    override val completed: Boolean = false,
    val content: String? = null
) : LectureUnit() {
    override fun withCompleted(newCompleted: Boolean): LectureUnit = copy(completed = newCompleted)
}
