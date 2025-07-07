package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed interface NotificationType

@Serializable
sealed interface CommunicationNotificationType : NotificationType

@Serializable
enum class StandalonePostCommunicationNotificationType : CommunicationNotificationType {
    @SerialName("newPostNotification")
    NEW_POST_NOTIFICATION,

    @SerialName("newAnnouncementNotification")
    NEW_ANNOUNCEMENT_NOTIFICATION
}

@Serializable
enum class ReplyPostCommunicationNotificationType : CommunicationNotificationType {
    @SerialName("newAnswerNotification")
    NEW_ANSWER_NOTIFICATION,

    @SerialName("newMentionNotification")
    NEW_MENTION_NOTIFICATION
}

@Serializable
enum class GeneralNotificationType(@StringRes val title: Int, @StringRes val body: Int) : NotificationType {
    @SerialName("newExerciseNotification")
    NEW_EXERCISE_NOTIFICATION(R.string.push_notification_title_exerciseReleased, R.string.push_notification_text_exerciseReleased),
    @SerialName("exerciseOpenForPracticeNotification")
    EXERCISE_OPEN_FOR_PRACTICE_NOTIFICATION(R.string.push_notification_title_exerciseOpenForPractice, R.string.push_notification_text_exercisePractice),
    @SerialName("exerciseAssessedNotification")
    EXERCISE_ASSESSED_NOTIFICATION(R.string.push_notification_title_exerciseSubmissionAssessed, R.string.push_notification_text_exerciseSubmissionAssessed),
    @SerialName("exerciseUpdatedNotification")
    EXERCISE_UPDATED_NOTIFICATION(R.string.push_notification_title_exerciseUpdated, R.string.push_notification_text_exerciseUpdated),
    @SerialName("quizExerciseStartedNotification")
    QUIZ_EXERCISE_STARTED_NOTIFICATION(R.string.push_notification_text_quizExerciseStarted, R.string.push_notification_text_quizExerciseStarted),
    @SerialName("attachmentChangedNotification")
    ATTACHMENT_CHANGED_NOTIFICATION(R.string.push_notification_title_attachmentChanged, R.string.push_notification_text_attachmentChange),
    @SerialName("newManualFeedbackRequestNotification")
    NEW_MANUAL_FEEDBACK_REQUEST_NOTIFICATION(R.string.push_notification_title_newManualFeedbackRequest, R.string.push_notification_text_newManualFeedbackRequest),
    @SerialName("duplicateTestCaseNotification")
    DUPLICATE_TEST_CASE_NOTIFICATION(R.string.push_notification_title_duplicateTestCase, R.string.push_notification_text_duplicateTestCase),
    @SerialName("newCpcPlagiarismCaseNotification")
    NEW_CPC_PLAGIARISM_CASE_NOTIFICATION(R.string.push_notification_title_newCpcPlagiarismCheck, R.string.push_notification_text_newCpcPlagiarismCheck),
    @SerialName("newPlagiarismCaseNotification")
    NEW_PLAGIARISM_CASE_NOTIFICATION(R.string.push_notification_title_newPlagiarismCaseStudent, R.string.push_notification_text_newPlagiarismCaseStudent),
    @SerialName("programmingBuildRunUpdateNotification")
    PROGRAMMING_BUILD_RUN_UPDATE_NOTIFICATION(R.string.push_notification_title_programmingBuildUpdate, R.string.push_notification_text_programmingBuildUpdate),
    @SerialName("programmingTestCasesChangedNotification")
    PROGRAMMING_TEST_CASES_CHANGED_NOTIFICATION(R.string.push_notification_title_programmingTestCasesChanged, R.string.push_notification_text_programmingTestCasesChanged),
    @SerialName("plagiarismCaseVerdictNotification")
    PLAGIARISM_CASE_VERDICT_NOTIFICATION(R.string.push_notification_title_plagiarismCaseVerdictStudent, R.string.push_notification_text_plagiarismCaseVerdictStudent),
    @SerialName("channelDeletedNotification")
    CHANNEL_DELETED_NOTIFICATION(R.string.push_notification_title_deleteChannel, R.string.push_notification_text_deleteChannel),
    @SerialName("addedToChannelNotification")
    ADDED_TO_CHANNEL_NOTIFICATION(R.string.push_notification_title_addUserChannel, R.string.push_notification_text_addUserChannel),
    @SerialName("removedFromChannelNotification")
    REMOVED_FROM_CHANNEL_NOTIFICATION(R.string.push_notification_title_removeUserChannel, R.string.push_notification_text_removeUserChannel),
    @SerialName("tutorialGroupAssignedNotification")
    TUTORIAL_GROUP_ASSIGNED_NOTIFICATION(R.string.push_notification_title_tutorialGroupAssigned, R.string.push_notification_text_tutorialGroupAssigned),
    @SerialName("tutorialGroupUnassignedNotification")
    TUTORIAL_GROUP_UNASSIGNED_NOTIFICATION(R.string.push_notification_title_tutorialGroupUnassigned, R.string.push_notification_text_tutorialGroupUnassigned),
    @SerialName("registeredToTutorialGroupNotification")
    REGISTERED_TO_TUTORIAL_GROUP_NOTIFICATION(R.string.push_notification_title_tutorialGroupRegistrationStudent, R.string.push_notification_text_tutorialGroupRegistrationStudent),
    @SerialName("deregisteredFromTutorialGroupNotification")
    DEREGISTERED_FROM_TUTORIAL_GROUP_NOTIFICATION(R.string.push_notification_title_tutorialGroupDeregistrationStudent, R.string.push_notification_text_tutorialGroupDeregistrationStudent),
    @SerialName("tutorialGroupDeletedNotification")
    TUTORIAL_GROUP_DELETED_NOTIFICATION(R.string.push_notification_title_tutorialGroupDeleted, R.string.push_notification_text_tutorialGroupDeleted)
}

fun GeneralNotificationType.isDisplayable(): Boolean {
    return when (this) {
        GeneralNotificationType.NEW_EXERCISE_NOTIFICATION,
        GeneralNotificationType.EXERCISE_OPEN_FOR_PRACTICE_NOTIFICATION,
        GeneralNotificationType.EXERCISE_ASSESSED_NOTIFICATION,
        GeneralNotificationType.EXERCISE_UPDATED_NOTIFICATION,
        GeneralNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION,
        GeneralNotificationType.ATTACHMENT_CHANGED_NOTIFICATION,
        GeneralNotificationType.NEW_MANUAL_FEEDBACK_REQUEST_NOTIFICATION,
        GeneralNotificationType.TUTORIAL_GROUP_ASSIGNED_NOTIFICATION,
        GeneralNotificationType.TUTORIAL_GROUP_UNASSIGNED_NOTIFICATION,
        GeneralNotificationType.REGISTERED_TO_TUTORIAL_GROUP_NOTIFICATION,
        GeneralNotificationType.DEREGISTERED_FROM_TUTORIAL_GROUP_NOTIFICATION,
        GeneralNotificationType.TUTORIAL_GROUP_DELETED_NOTIFICATION -> true

        else -> false
    }
    //TODO: fix missing text and compare with iOS
}

@Serializable
data object UnknownNotificationType : NotificationType

object NotificationTypeSerializer : KSerializer<NotificationType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NotificationType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): NotificationType {
        val value = decoder.decodeString()

        return StandalonePostCommunicationNotificationType.entries.find { it.serialName == value }
            ?: ReplyPostCommunicationNotificationType.entries.find { it.serialName == value }
            ?: GeneralNotificationType.entries.find { it.serialName == value }
            ?: UnknownNotificationType
    }

    override fun serialize(encoder: Encoder, value: NotificationType) {
        when (value) {
            is StandalonePostCommunicationNotificationType -> encoder.encodeString(value.serialName)
            is ReplyPostCommunicationNotificationType -> encoder.encodeString(value.serialName)
            is GeneralNotificationType -> encoder.encodeString(value.serialName)
            UnknownNotificationType -> encoder.encodeString("unknown")
        }
    }

    // Extension to get SerialName from enum
    private val Enum<*>.serialName: String
        get() = javaClass.getField(name).getAnnotation(SerialName::class.java)?.value ?: name
}
