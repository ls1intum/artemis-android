package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.push.R

object PushNotificationLocalization {

    @Composable
    fun getGroupName(groupName: String): String {
        val id = when (groupName) {
            "weekly-summary" -> R.string.push_notification_settings_group_weeklySummary
            "exercise-notification" -> R.string.push_notification_settings_group_exerciseNotifications
            "lecture-notification" -> R.string.push_notification_settings_group_lectureNotifications
            "tutorial-group-notification" -> R.string.push_notification_settings_group_tutorialGroupNotifications
            "course-wide-discussion" -> R.string.push_notification_settings_group_courseWideDiscussionNotifications
            "tutor-notification" -> R.string.push_notification_settings_group_tutorNotifications
            "editor-notification" -> R.string.push_notification_settings_group_editorNotifications
            "exam-notification" -> R.string.push_notification_settings_group_examNotifications
            "instructor-notification" -> R.string.push_notification_settings_group_instructorNotifications
            "user-notification" -> R.string.push_notification_settings_group_conversationNotification
            else -> null
        }

        return id?.let { stringResource(id = it) } ?: groupName
    }

    @Composable
    fun getSettingName(settingName: String): String {
        val id = when (settingName) {
            "basic-weekly-summary" -> R.string.push_notification_settings_setting_basicWeeklySummary
            "exercise-released" -> R.string.push_notification_settings_setting_exerciseReleased
            "exercise-open-for-practice" -> R.string.push_notification_settings_setting_exerciseOpenForPractice
            "exercise-submission-assessed" -> R.string.push_notification_settings_setting_exerciseSubmissionAssessed
            "attachment-changes" -> R.string.push_notification_settings_setting_attachmentChanges
            "new-exercise-post" -> R.string.push_notification_settings_setting_newExercisePost
            "new-reply-for-exercise-post" -> R.string.push_notification_settings_setting_newReplyForExercisePost
            "new-lecture-post" -> R.string.push_notification_settings_setting_newLecturePost
            "new-reply-for-lecture-post" -> R.string.push_notification_settings_setting_newReplyForLecturePost
            "new-course-post" -> R.string.push_notification_settings_setting_newCoursePost
            "new-reply-for-course-post" -> R.string.push_notification_settings_setting_newReplyForCoursePost
            "new-announcement-post" -> R.string.push_notification_settings_setting_newAnnouncementPost
            "course-and-exam-archiving-started" -> R.string.push_notification_settings_setting_courseAndExamArchivingStarted
            "file-submission-successful" -> R.string.push_notification_settings_setting_fileSubmissionSuccessful
            "programming-test-cases-changed" -> R.string.push_notification_settings_setting_programmingTestCasesChanged
            "new-reply-for-exam-post" -> R.string.push_notification_settings_setting_newExamReply
            "new-exam-post" -> R.string.push_notification_settings_setting_newExamPost
            "tutorial-group-registration" -> R.string.push_notification_settings_setting_registrationTutorialGroup
            "tutorial-group-delete-update" -> R.string.push_notification_settings_setting_tutorialGroupUpdateDelete
            "tutorial-group-assign-unassign" -> R.string.push_notification_settings_setting_assignUnassignTutorialGroup
            "quiz_start_reminder" -> R.string.push_notification_settings_setting_quizStartReminder
            "conversation-message" -> R.string.push_notification_setting_setting_newConversationMessages
            "new-reply-in-conversation" -> R.string.push_notification_setting_setting_newConversationReplies
            "user-mention" -> R.string.push_notification_setting_setting_conversationUserMention
            else -> null
        }

        return id?.let { stringResource(id = it) } ?: settingName
    }

    @Composable
    fun getSettingDescription(settingName: String): String? {
        val id = when (settingName) {
            "basic-weekly-summary" -> R.string.push_notification_setting_setting_description_basicWeeklySummaryDescription
            "exercise-released" -> R.string.push_notification_setting_setting_description_exerciseReleasedDescription
            "exercise-open-for-practice" -> R.string.push_notification_setting_setting_description_exerciseOpenForPracticeDescription
            "exercise-submission-assessed" -> R.string.push_notification_setting_setting_description_exerciseSubmissionAssessedDescription
            "attachment-changes" -> R.string.push_notification_setting_setting_description_attachmentChangesDescription
            "new-exercise-post" -> R.string.push_notification_setting_setting_description_newExercisePostDescription
            "new-reply-for-exercise-post" -> R.string.push_notification_setting_setting_description_newReplyForExercisePostDescription
            "new-lecture-post" -> R.string.push_notification_setting_setting_description_newLecturePostDescription
            "new-reply-for-lecture-post" -> R.string.push_notification_setting_setting_description_newReplyForLecturePostDescription
            "new-course-post" -> R.string.push_notification_setting_setting_description_newCoursePostDescription
            "new-reply-for-course-post" -> R.string.push_notification_setting_setting_description_newReplyForCoursePostDescription
            "new-announcement-post" -> R.string.push_notification_setting_setting_description_newAnnouncementPostDescription
            "course-and-exam-archiving-started" -> R.string.push_notification_setting_setting_description_courseAndExamArchivingStartedDescription
            "file-submission-successful" -> R.string.push_notification_setting_setting_description_fileSubmissionSuccessfulDescription
            "programming-test-cases-changed" -> R.string.push_notification_setting_setting_description_programmingTestCasesChangedDescription
            "new-reply-for-exam-post" -> R.string.push_notification_settings_setting_newExamReplyDescription
            "new-exam-post" -> R.string.push_notification_settings_setting_newExamPostDescription
            "tutorial-group-registration" -> R.string.push_notification_setting_setting_description_registrationTutorialGroupStudentDescription
            "tutorial-group-delete-update" -> R.string.push_notification_setting_setting_description_tutorialGroupUpdateDeleteDescription
            "tutorial-group-assign-unassign" -> R.string.push_notification_setting_setting_description_assignUnassignTutorialGroupDescription
            "quiz_start_reminder" -> R.string.push_notification_setting_setting_description_quizStartReminder
            "user-mention" -> R.string.push_notification_settings_setting_conversationUserMentionDescription
            "new-reply-in-conversation" -> R.string.push_notification_setting_setting_newConversationRepliesDescription
            "conversation-message" -> R.string.push_notification_setting_setting_newConversationMessagesDescription
            else -> null
        }

        return id?.let { stringResource(id = it) }
    }
}