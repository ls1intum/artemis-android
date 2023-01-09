package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun PushNotificationSettingsUi(
    modifier: Modifier,
    viewModel: PushNotificationSettingsViewModel = koinViewModel()
) {
    val settingsByGroupDataStore: DataState<List<PushNotificationSettingsViewModel.NotificationCategory>> by viewModel.currentSettingsByGroup.collectAsState()
    val arePushNotificationEnabled by viewModel.arePushNotificationsEnabled.collectAsState(initial = false)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReceivePushNotificationsSwitch(
            modifier = Modifier.fillMaxWidth(),
            isChecked = arePushNotificationEnabled,
            onCheckedChange = viewModel::updatePushNotificationEnabled
        )

        PushNotificationSettingCategoriesListUi(
            modifier = Modifier.fillMaxWidth(),
            settingsByGroupDataStore = settingsByGroupDataStore,
            onUpdate = { setting, webapp, email ->
                viewModel.updateSettingsEntry(
                    setting.settingId,
                    email,
                    webapp
                )
            },
            onRequestReload = viewModel::requestReloadSettings
        )
    }
}

@Composable
private fun ReceivePushNotificationsSwitch(
    modifier: Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.push_notification_settings_receive_information),
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.push_notification_settings_receive_label),
                style = MaterialTheme.typography.bodyLarge
            )

            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun PushNotificationSyncChangesDialog(onDismissRequest: () -> Unit) {
    TextAlertDialog(
        title = stringResource(id = R.string.push_notification_settings_sync_dialog_title),
        text = stringResource(id = R.string.push_notification_settings_sync_dialog_message),
        confirmButtonText = null,
        dismissButtonText = stringResource(id = R.string.push_notification_settings_sync_dialog_dismiss),
        onPressPositiveButton = { },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun PushNotificationSyncFailedDialog(onDismissRequest: () -> Unit) {
    TextAlertDialog(
        title = stringResource(id = R.string.push_notification_settings_sync_failed_dialog_title),
        text = stringResource(id = R.string.push_notification_settings_sync_failed_dialog_message),
        confirmButtonText = stringResource(id = R.string.push_notification_settings_sync_failed_dialog_positive),
        dismissButtonText = null,
        onPressPositiveButton = onDismissRequest,
        onDismissRequest = onDismissRequest
    )
}