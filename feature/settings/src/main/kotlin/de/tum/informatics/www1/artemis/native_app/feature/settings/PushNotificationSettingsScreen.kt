package de.tum.informatics.www1.artemis.native_app.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.PushNotificationSettingsUi
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.PushNotificationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.PushNotificationSyncChangesDialog
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.PushNotificationSyncFailedDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import kotlinx.coroutines.Job
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PushNotificationSettingsScreen(modifier: Modifier, onNavigateBack: () -> Unit) {
    val viewModel: PushNotificationSettingsViewModel = koinViewModel()
    val isDirty: Boolean by viewModel.isDirty.collectAsState(initial = false)

    // Set if currently syncing changes with server
    var syncChangesJob: Job? by remember { mutableStateOf(null) }
    var displaySyncFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.notification_settings_screen_title)
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isDirty,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        syncChangesJob = viewModel.saveSettings { isSuccessful ->
                            syncChangesJob = null
                            if (!isSuccessful) {
                                displaySyncFailedDialog = true
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                }
            }
        }
    ) { padding ->
        PushNotificationSettingsUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            viewModel = viewModel
        )

        if (syncChangesJob != null) {
            PushNotificationSyncChangesDialog(
                onDismissRequest = {
                    syncChangesJob?.cancel()
                    syncChangesJob = null
                }
            )
        }

        if (displaySyncFailedDialog) {
            PushNotificationSyncFailedDialog { displaySyncFailedDialog = false }
        }
    }
}