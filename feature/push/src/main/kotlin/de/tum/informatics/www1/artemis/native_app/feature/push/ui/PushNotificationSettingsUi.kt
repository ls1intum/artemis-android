package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.linkTextColor
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import kotlinx.coroutines.Job
import org.koin.androidx.compose.koinViewModel

@Composable
fun PushNotificationSettingsUi(
    modifier: Modifier,
    viewModel: PushNotificationSettingsViewModel = koinViewModel()
) {
    val settingsByGroupDataStore: DataState<List<PushNotificationSettingsViewModel.NotificationCategory>> by viewModel.currentSettingsByGroup.collectAsState()
    val arePushNotificationEnabled by viewModel.arePushNotificationsEnabled.collectAsState()

    var updatePushNotificationEnabledJob: Job? by remember { mutableStateOf(null) }
    var displayUpdatePushNotificationsFailed: Boolean by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReceivePushNotificationsSwitch(
            modifier = Modifier.fillMaxWidth(),
            isChecked = arePushNotificationEnabled,
            onCheckedChange = { isEnabled ->
                updatePushNotificationEnabledJob = viewModel
                    .updatePushNotificationEnabled(isEnabled) { wasSuccessful ->
                        updatePushNotificationEnabledJob = null

                        if (!wasSuccessful) {
                            displayUpdatePushNotificationsFailed = true
                        }
                    }
            }
        )

        AnimatedVisibility(visible = arePushNotificationEnabled) {
            PushNotificationSettingCategoriesListUi(
                modifier = Modifier.fillMaxWidth(),
                settingsByGroupDataStore = settingsByGroupDataStore,
                onUpdate = { setting, webapp, email, push ->
                    viewModel.updateSettingsEntry(
                        setting.settingId,
                        email,
                        webapp,
                        push
                    )
                },
                onRequestReload = viewModel::requestReloadSettings
            )
        }
    }

    if (updatePushNotificationEnabledJob != null) {
        PushNotificationSyncChangesDialog {
            updatePushNotificationEnabledJob?.cancel()
            updatePushNotificationEnabledJob = null
        }
    }

    if (displayUpdatePushNotificationsFailed) {
        PushNotificationSyncFailedDialog {
            displayUpdatePushNotificationsFailed = false
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ReceivePushNotificationsSwitch(
    modifier: Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val notificationPermissionState: PermissionState? = if (Build.VERSION.SDK_INT >= 33) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else null

    var enableOnPermissionGranted: Boolean by rememberSaveable { mutableStateOf(false) }

    // Effect that will automatically enable notifications once the permission has been granted
    LaunchedEffect(enableOnPermissionGranted, notificationPermissionState?.status?.isGranted) {
        if (enableOnPermissionGranted && notificationPermissionState?.status?.isGranted == true) {
            enableOnPermissionGranted = false
            onCheckedChange(true)
        }
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var isInfoTextExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
            var hasOverflow: Boolean by rememberSaveable { mutableStateOf(false) }

            Column {
                Text(
                    text = stringResource(id = R.string.push_notification_settings_receive_information),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = if (isInfoTextExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        hasOverflow = result.hasVisualOverflow
                    }
                )

                if (hasOverflow) {
                    Box(
                        Modifier.clickable { isInfoTextExpanded = true }
                    ) {
                        Text(
                            text = AnnotatedString(stringResource(id = R.string.push_notification_settings_receive_information_expand)),
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.linkTextColor)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.push_notification_settings_receive_label),
                    style = MaterialTheme.typography.bodyLarge
                )

                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked ->
                        // Handle the cases where we need to ask thew user for permission
                        val canShowPermissions =
                            notificationPermissionState == null || notificationPermissionState.status.isGranted
                        if (isChecked) {
                            if (canShowPermissions) {
                                onCheckedChange(true)
                            } else {
                                // When the user grants the permission, automatically enable notifications
                                enableOnPermissionGranted = true

                                // Ask for the permission
                                notificationPermissionState?.launchPermissionRequest()
                            }
                        } else onCheckedChange(false)
                    }
                )
            }
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