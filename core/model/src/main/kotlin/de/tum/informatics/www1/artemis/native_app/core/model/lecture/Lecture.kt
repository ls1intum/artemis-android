package de.tum.informatics.www1.artemis.native_app.core.model.lecture

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Lecture(
    val id: Int,
    val title: String,
    val description: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val attachments: List<Attachment> = emptyList()
)