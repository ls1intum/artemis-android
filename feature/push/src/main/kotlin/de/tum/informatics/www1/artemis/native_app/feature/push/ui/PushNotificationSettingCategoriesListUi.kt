package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.setting

internal const val TEST_TAG_PUSH_SWITCH= "TEST_TAG_PUSH_SWITCH"

internal fun testTagForSettingCategory(categoryId: String) = "notification category $categoryId"

internal fun testTagForSetting(settingId: String) = "notification setting $settingId"

@Composable
internal fun PushNotificationSettingCategoriesListUi(
    modifier: Modifier,
    settingsByGroupDataStore: DataState<List<PushNotificationSettingsViewModel.NotificationCategory>>,
    onUpdate: (PushNotificationSetting, webapp: Boolean?, email: Boolean?, push: Boolean?) -> Unit,
    onRequestReload: () -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = settingsByGroupDataStore,
        loadingText = stringResource(id = R.string.push_notification_settings_loading),
        failureText = stringResource(id = R.string.push_notification_settings_failure),
        retryButtonText = stringResource(id = R.string.push_notification_settings_try_again),
        onClickRetry = onRequestReload
    ) { settingsByGroup ->
        PushNotificationSettingsList(
            modifier = Modifier.fillMaxSize(),
            settingCategories = settingsByGroup,
            onUpdate = onUpdate
        )
    }
}

@Composable
private fun PushNotificationSettingsList(
    modifier: Modifier,
    settingCategories: List<PushNotificationSettingsViewModel.NotificationCategory>,
    onUpdate: (PushNotificationSetting, webapp: Boolean?, email: Boolean?, push: Boolean?) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        settingCategories.forEach { category ->
            Card(
                modifier = modifier
                    .testTag(testTagForSettingCategory(category.categoryId)),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = getLocalizedNotificationGroupName(groupName = category.categoryId),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    category.settings.forEachIndexed { settingIndex, pushNotificationSetting ->
                        PushNotificationSettingEntry(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(testTagForSetting(pushNotificationSetting.settingId)),
                            setting = pushNotificationSetting,
                            onUpdate = { webapp, email, push ->
                                onUpdate(
                                    pushNotificationSetting,
                                    webapp,
                                    email,
                                    push
                                )
                            }
                        )

                        if (settingIndex != category.settings.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PushNotificationSettingEntry(
    modifier: Modifier,
    setting: PushNotificationSetting,
    onUpdate: (webapp: Boolean?, email: Boolean?, push: Boolean?) -> Unit
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getLocalizedNotificationSettingName(settingName = setting.setting),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            val description =
                getLocalizedNotificationSettingDescription(settingName = setting.setting)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // This is commented out because currently we only want to display push settings.
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .horizontalScroll(rememberScrollState())
//            ) {
//
//            if (setting.webapp != null) {
//                TextCheckBox(
//                    modifier = Modifier,
//                    isChecked = setting.webapp,
//                    text = stringResource(id = R.string.push_notification_settings_label_webapp),
//                    onCheckedChanged = { onUpdate(it, setting.email, setting.push) }
//                )
//            }
//
//            if (setting.email != null) {
//                TextCheckBox(
//                    modifier = Modifier,
//                    isChecked = setting.email,
//                    text = stringResource(id = R.string.push_notification_settings_label_email),
//                    onCheckedChanged = { onUpdate(setting.webapp, it, setting.push) }
//                )
//            }
//            }
        }

        if (setting.push != null) {
            Switch(
                modifier = Modifier
                    .scale(0.9f)
                    .testTag(TEST_TAG_PUSH_SWITCH),
                checked = setting.push,
                onCheckedChange = { onUpdate(setting.webapp, setting.email, it) }
            )
        }
    }
}

//@Composable
//private fun TextCheckBox(
//    modifier: Modifier,
//    isChecked: Boolean,
//    text: String,
//    onCheckedChanged: (Boolean) -> Unit
//) {
//    Row(
//        modifier = modifier,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Checkbox(checked = isChecked, onCheckedChange = onCheckedChanged)
//
//        Text(
//            text = text,
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}

@Composable
private fun getLocalizedNotificationGroupName(groupName: String): String {
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
private fun getLocalizedNotificationSettingName(settingName: String): String {
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
        "data-export-failed" -> R.string.push_notification_setting_setting_conversationDataExportFailed
        "data-export-created" -> R.string.push_notification_setting_setting_conversationDataExportCreated
        else -> null
    }

    return id?.let { stringResource(id = it) } ?: settingName
}

@Composable
private fun getLocalizedNotificationSettingDescription(settingName: String): String? {
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