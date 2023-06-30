package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Lecture(
    val id: Long? = 0,
    val title: String = "",
    val description: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val attachments: List<Attachment> = emptyList(),
    val lectureUnits: List<LectureUnit> = emptyList(),
    val course: Course? = null
)