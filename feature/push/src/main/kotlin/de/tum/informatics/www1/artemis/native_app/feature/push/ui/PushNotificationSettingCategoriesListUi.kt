package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.setting

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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = getLocalizedNotificationGroupName(groupName = category.categoryId),
                    style = MaterialTheme.typography.headlineMedium
                )

                category.settings.forEachIndexed { settingIndex, pushNotificationSetting ->
                    PushNotificationSettingEntry(
                        modifier = Modifier.fillMaxWidth(),
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
                        Divider()
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
    Column(modifier = modifier) {
        Text(
            text = getLocalizedNotificationSettingName(settingName = setting.setting),
            style = MaterialTheme.typography.bodyLarge
        )

        val description =
            getLocalizedNotificationSettingDescription(settingName = setting.setting)
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            // This is commented out because currently we only want to display push settings.
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

            if (setting.push != null) {
                TextCheckBox(
                    modifier = Modifier,
                    isChecked = setting.push,
                    text = stringResource(id = R.string.push_notification_settings_label_push),
                    onCheckedChanged = { onUpdate(setting.webapp, setting.email, it) }
                )
            }
        }
    }
}

@Composable
private fun TextCheckBox(
    modifier: Modifier,
    isChecked: Boolean,
    text: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChanged)

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

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
        "instructor-notification" -> R.string.push_notification_settings_group_instructorNotifications
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
        "tutorial-group-registration" -> R.string.push_notification_settings_setting_registrationTutorialGroup
        "tutorial-group-delete-update" -> R.string.push_notification_settings_setting_tutorialGroupUpdateDelete
        "tutorial-group-assign-unassign" -> R.string.push_notification_settings_setting_assignUnassignTutorialGroup
        "quiz_start_reminder" -> R.string.push_notification_settings_setting_quizStartReminder
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
        "tutorial-group-registration" -> R.string.push_notification_setting_setting_description_registrationTutorialGroupStudentDescription
        "tutorial-group-delete-update" -> R.string.push_notification_setting_setting_description_tutorialGroupUpdateDeleteDescription
        "tutorial-group-assign-unassign" -> R.string.push_notification_setting_setting_description_assignUnassignTutorialGroupDescription
        "quiz_start_reminder" -> R.string.push_notification_setting_setting_description_quizStartReminder
        else -> null
    }

    return id?.let { stringResource(id = it) }
}