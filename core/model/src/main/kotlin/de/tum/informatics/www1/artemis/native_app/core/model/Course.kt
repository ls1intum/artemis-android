package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representation of a single course.
 */
@Serializable
data class Course(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    @SerialName("courseIcon") val courseIconPath: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val lectures: List<Lecture> = emptyList(),
    val semester: String = "",
    val registrationConfirmationMessage: String = "",
    val accuracyOfScores: Float = 1f,
    val postsEnabled: Boolean = true,
    val color: String? = null,
    val instructorGroupName: String = "",
    val studentGroupName: String = "",
    val teachingAssistantGroupName: String = "",
    val editorGroupName: String = ""
)
