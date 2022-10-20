package de.tum.informatics.www1.artemis.native_app.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representation of a single course.
 */
@Serializable
data class Course(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("courseIcon") val courseIconPath: String
)