package de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class LectureUnit {
    abstract val id: Long
    abstract val name: String?
    abstract val releaseDate: Instant?
    abstract val visibleToStudents: Boolean
    abstract val completed: Boolean
}
