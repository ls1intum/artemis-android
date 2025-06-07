package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.R
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date


data class CourseNotification(
    val notificationType: NotificationType,
    val notificationId: Int,
    val courseId: Int,
    val creationDate: Date,
    val category: NotificationCategory,
    val status: NotificationStatus,
    val notification: ArtemisNotification<NotificationType>
) {
    val id: Int get() = notificationId
}

enum class NotificationCategory {
    COMMUNICATION,
    GENERAL,
    UNKNOWN
}

enum class NotificationStatus {
    UNSEEN,
    SEEN,
    UNKNOWN
}

@Serializable
enum class CourseNotificationType {
    // Communication
    @SerialName("addedToChannelNotification")
    ADDED_TO_CHANNEL_NOTIFICATION,

    @SerialName("channelDeletedNotification")
    CHANNEL_DELETED_NOTIFICATION,

    @SerialName("newAnnouncementNotification")
    NEW_ANNOUNCEMENT_NOTIFICATION,

    @SerialName("newAnswerNotification")
    NEW_ANSWER_NOTIFICATION,

    @SerialName("newMentionNotification")
    NEW_MENTION_NOTIFICATION,

    @SerialName("newPostNotification")
    NEW_POST_NOTIFICATION,

    @SerialName("removedFromChannelNotification")
    REMOVED_FROM_CHANNEL_NOTIFICATION,

    // General
    @SerialName("attachmentChangedNotification")
    ATTACHMENT_CHANGED_NOTIFICATION,

    @SerialName("deregisteredFromTutorialGroupNotification")
    DEREGISTERED_FROM_TUTORIAL_GROUP_NOTIFICATION,

    @SerialName("duplicateTestCaseNotification")
    DUPLICATE_TEST_CASE_NOTIFICATION,

    @SerialName("exerciseAssessedNotification")
    EXERCISE_ASSESSED_NOTIFICATION,

    @SerialName("exerciseOpenForPracticeNotification")
    EXERCISE_OPEN_FOR_PRACTICE_NOTIFICATION,

    @SerialName("exerciseUpdatedNotification")
    EXERCISE_UPDATED_NOTIFICATION,

    @SerialName("newCpcPlagiarismCaseNotification")
    NEW_CPC_PLAGIARISM_CASE_NOTIFICATION,

    @SerialName("newExerciseNotification")
    NEW_EXERCISE_NOTIFICATION,

    @SerialName("newManualFeedbackRequestNotification")
    NEW_MANUAL_FEEDBACK_REQUEST_NOTIFICATION,

    @SerialName("newPlagiarismCaseNotification")
    NEW_PLAGIARISM_CASE_NOTIFICATION,

    @SerialName("plagiarismCaseVerdictNotification")
    PLAGIARISM_CASE_VERDICT_NOTIFICATION,

    @SerialName("programmingBuildRunUpdateNotification")
    PROGRAMMING_BUILD_RUN_UPDATE_NOTIFICATION,

    @SerialName("programmingTestCasesChangedNotification")
    PROGRAMMING_TEST_CASES_CHANGED_NOTIFICATION,

    @SerialName("quizExerciseStartedNotification")
    QUIZ_EXERCISE_STARTED_NOTIFICATION,

    @SerialName("registeredToTutorialGroupNotification")
    REGISTERED_TO_TUTORIAL_GROUP_NOTIFICATION,

    @SerialName("tutorialGroupAssignedNotification")
    TUTORIAL_GROUP_ASSIGNED_NOTIFICATION,

    @SerialName("tutorialGroupDeletedNotification")
    TUTORIAL_GROUP_DELETED_NOTIFICATION,

    @SerialName("tutorialGroupUnassignedNotification")
    TUTORIAL_GROUP_UNASSIGNED_NOTIFICATION,

    UNKNOWN;

    companion object {
        fun fromString(value: String): CourseNotificationType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: UNKNOWN
    }
}


@Composable
fun CourseNotificationType.settingsTitle(): String = when (this) {
    CourseNotificationType.ADDED_TO_CHANNEL_NOTIFICATION -> stringResource(R.string.add_channel_settings_name)
    CourseNotificationType.CHANNEL_DELETED_NOTIFICATION -> stringResource(R.string.delete_channel_settings_name)
    CourseNotificationType.NEW_ANNOUNCEMENT_NOTIFICATION -> stringResource(R.string.new_announcement_settings_name)
    CourseNotificationType.NEW_ANSWER_NOTIFICATION -> stringResource(R.string.new_reply_settings_name)
    CourseNotificationType.NEW_MENTION_NOTIFICATION -> stringResource(R.string.new_mention_settings_name)
    CourseNotificationType.NEW_POST_NOTIFICATION -> stringResource(R.string.new_message_settings_name)
    CourseNotificationType.REMOVED_FROM_CHANNEL_NOTIFICATION -> stringResource(R.string.remove_channel_settings_name)
    CourseNotificationType.ATTACHMENT_CHANGED_NOTIFICATION -> stringResource(R.string.attachment_changed_settings_name)
    CourseNotificationType.DEREGISTERED_FROM_TUTORIAL_GROUP_NOTIFICATION -> stringResource(R.string.tutorial_deregistered_settings_name)
    CourseNotificationType.DUPLICATE_TEST_CASE_NOTIFICATION -> stringResource(R.string.duplicate_test_settings_name)
    CourseNotificationType.EXERCISE_ASSESSED_NOTIFICATION -> stringResource(R.string.exercise_assessed_settings_name)
    CourseNotificationType.EXERCISE_OPEN_FOR_PRACTICE_NOTIFICATION -> stringResource(R.string.exercise_open_for_practice_settings_name)
    CourseNotificationType.EXERCISE_UPDATED_NOTIFICATION -> stringResource(R.string.exercise_updated_settings_name)
    CourseNotificationType.NEW_CPC_PLAGIARISM_CASE_NOTIFICATION -> stringResource(R.string.new_similarity_settings_name)
    CourseNotificationType.NEW_EXERCISE_NOTIFICATION -> stringResource(R.string.exercise_released_settings_name)
    CourseNotificationType.NEW_MANUAL_FEEDBACK_REQUEST_NOTIFICATION -> stringResource(R.string.feedback_request_settings_name)
    CourseNotificationType.NEW_PLAGIARISM_CASE_NOTIFICATION -> stringResource(R.string.new_plagiarism_settings_name)
    CourseNotificationType.PLAGIARISM_CASE_VERDICT_NOTIFICATION -> stringResource(R.string.plagiarism_verdict_settings_name)
    CourseNotificationType.PROGRAMMING_BUILD_RUN_UPDATE_NOTIFICATION -> stringResource(R.string.build_update_settings_name)
    CourseNotificationType.PROGRAMMING_TEST_CASES_CHANGED_NOTIFICATION -> stringResource(R.string.test_case_changed_settings_name)
    CourseNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION -> stringResource(R.string.quiz_started_settings_name)
    CourseNotificationType.REGISTERED_TO_TUTORIAL_GROUP_NOTIFICATION -> stringResource(R.string.tutorial_registered_settings_name)
    CourseNotificationType.TUTORIAL_GROUP_ASSIGNED_NOTIFICATION -> stringResource(R.string.tutorial_assigned_settings_name)
    CourseNotificationType.TUTORIAL_GROUP_DELETED_NOTIFICATION -> stringResource(R.string.tutorial_deleted_settings_name)
    CourseNotificationType.TUTORIAL_GROUP_UNASSIGNED_NOTIFICATION -> stringResource(R.string.tutorial_unassigned_settings_name)
    CourseNotificationType.UNKNOWN -> ""
}

