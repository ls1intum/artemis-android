package de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("attachmentType")
sealed class Attachment {
    abstract val id: Int
    abstract val name: String
    abstract val visibleToStudents: Boolean
}