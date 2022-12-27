package de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class LectureUnitUnknown(
    override val id: Long = 0,
    override val name: String? = null,
    override val releaseDate: Instant? = null,
    override val visibleToStudents: Boolean = true,
    override val completed: Boolean = false,
) : LectureUnit()
