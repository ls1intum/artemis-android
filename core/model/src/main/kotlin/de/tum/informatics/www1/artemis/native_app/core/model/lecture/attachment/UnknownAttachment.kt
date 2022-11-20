package de.tum.informatics.www1.artemis.native_app.core.model.lecture.attachment

import kotlinx.serialization.Serializable

/**
 * Default deserializer when the app parses an unknown attachment type.
 */
@Serializable
class UnknownAttachment(
    override val id: Int,
    override val name: String,
    override val visibleToStudents: Boolean
) : Attachment()