package de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Default deserializer when the app parses an unknown attachment type.
 */
@Parcelize
@Serializable
class UnknownAttachment(
    override val id: Int,
    override val name: String,
    override val visibleToStudents: Boolean
) : Attachment()