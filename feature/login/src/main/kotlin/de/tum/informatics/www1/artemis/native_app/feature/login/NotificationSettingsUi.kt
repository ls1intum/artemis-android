package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.account.R
import kotlinx.coroutines.Job
import org.koin.androidx.compose.koinViewModel

/**
 * Display UI so the user can select if they want to receive notifications and what notifications to receive.
 */
@Composable
internal fun NotificationSettingsUi(modifier: Modifier, onDone: () -> Unit) {
    val viewModel: de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel = koinViewModel()
    val isDirty by viewModel.isDirty.collectAsState(initial = false)

    var saveJob: Job? by remember { mutableStateOf(null) }
    var displaySyncFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(text = stringResource(id = R.string.push_notification_settings_title))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // If changes have been made, these need to be synced first.
                    if (isDirty) {
                        saveJob = viewModel.saveSettings { successful ->
                            if (successful) {
                                onDone()
                            } else {
                                displaySyncFailedDialog = true
                            }
                        }
                    } else onDone()
                },
                text = {
                    Text(
                        text = stringResource(
                            id =
                            if (isDirty) R.string.push_notification_settings_fab_text_with_save
                            else R.string.push_notification_settings_fab_text_without_save
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (isDirty) Icons.Default.Save else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsUi(
                modifier = Modifier.fillMaxWidth(),
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(64.dp))
        }

        if (saveJob != null) {
            de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSyncChangesDialog(
                onDismissRequest = {
                    saveJob?.cancel()
                    saveJob = null
                }
            )
        }

        if (displaySyncFailedDialog) {
            de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSyncFailedDialog {
                displaySyncFailedDialog = false
            }
        }
    }
}