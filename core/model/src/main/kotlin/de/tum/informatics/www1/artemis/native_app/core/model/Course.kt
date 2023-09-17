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
    val id: Long? = 0,
    val title: String = "",
    val shortName: String = "",
    val description: String = "",
    @SerialName("courseIcon") val courseIconPath: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val lectures: List<Lecture> = emptyList(),
    val semester: String = "",
    val registrationConfirmationMessage: String = "",
    val accuracyOfScores: Float = 1f,
    val courseInformationSharingConfiguration: CourseInformationSharingConfiguration = CourseInformationSharingConfiguration.DISABLED,
    val color: String? = null,
    val instructorGroupName: String = "",
    val studentGroupName: String = "",
    val teachingAssistantGroupName: String = "",
    val editorGroupName: String = "",
    val testCourse: Boolean = false,
    val courseInformationSharingMessagingCodeOfConduct: String = ""
) {
    enum class CourseInformationSharingConfiguration(val supportsMessaging: Boolean) {
        COMMUNICATION_AND_MESSAGING(true),
        COMMUNICATION_ONLY(false),
        MESSAGING_ONLY(true),
        DISABLED(false)
    }
}
