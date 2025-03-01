package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Scaling
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.setting

private const val TEST_TAG_PUSH_SWITCH = "TEST_TAG_PUSH_SWITCH"
internal fun testTagForSwitch(settingId: String) = TEST_TAG_PUSH_SWITCH + settingId

internal fun testTagForSettingCategory(categoryId: String) = "notification category $categoryId"

internal fun testTagForSetting(settingId: String) = "notification setting $settingId"

@Composable
internal fun PushNotificationSettingCategoriesListUi(
    modifier: Modifier,
    settingsByGroupDataState: DataState<List<PushNotificationSettingsViewModel.NotificationCategory>>,
    onUpdate: (PushNotificationSetting, webapp: Boolean?, email: Boolean?, push: Boolean?) -> Unit,
    onRequestReload: () -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = settingsByGroupDataState,
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
                modifier = modifier,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag(testTagForSettingCategory(category.categoryId)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = PushNotificationLocalization.getGroupName(groupName = category.categoryId),
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

        Spacer(modifier = Modifier.height(32.dp))
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
                text = PushNotificationLocalization.getSettingName(settingName = setting.setting),
                style = MaterialTheme.typography.bodyLarge,
            )

            val description =
                PushNotificationLocalization.getSettingDescription(settingName = setting.setting)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
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
                    .scale(Scaling.SWITCH)
                    .testTag(testTagForSwitch(setting.settingId)),
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
