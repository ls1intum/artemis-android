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
    val enrollmentConfirmationMessage: String = "",
    val accuracyOfScores: Float = 1f,
    val courseInformationSharingConfiguration: CourseInformationSharingConfiguration = CourseInformationSharingConfiguration.DISABLED,
    val color: String? = null,
    val testCourse: Boolean = false,
    val courseInformationSharingMessagingCodeOfConduct: String = "",
    /**
     * Not sent by the server. Artemis 10 answers tab availability from
     * "courses/{courseId}/available-tabs" instead of carrying flags on the course, so this is filled
     * in by [de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService.getCourseWithContent].
     */
    val faqEnabled: Boolean = false,
) {
    enum class CourseInformationSharingConfiguration(val supportsMessaging: Boolean) {
        COMMUNICATION_AND_MESSAGING(true),
        COMMUNICATION_ONLY(false),
        MESSAGING_ONLY(true),
        DISABLED(false)
    }
}
