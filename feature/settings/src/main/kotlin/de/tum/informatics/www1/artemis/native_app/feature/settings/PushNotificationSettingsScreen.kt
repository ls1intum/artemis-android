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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsUi
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSyncFailedDialog
import kotlinx.coroutines.Job
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PushNotificationSettingsScreen(modifier: Modifier, onNavigateBack: () -> Unit) {
    val viewModel: PushNotificationSettingsViewModel = koinViewModel()
    val isDirty: Boolean by viewModel.isDirty.collectAsState(initial = false)

    // Set if currently syncing changes with server
    var syncChangesJob: Job? by remember { mutableStateOf(null) }
    var displaySyncFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = syncChangesJob) {
        syncChangesJob?.let {
            it.join()
            syncChangesJob = null
        }
    }

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
                        syncChangesJob?.cancel()

                        syncChangesJob = viewModel.saveSettings { isSuccessful ->
                            syncChangesJob = null
                            if (!isSuccessful) {
                                displaySyncFailedDialog = true
                            }
                        }
                    }
                ) {
                    if (syncChangesJob == null) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    } else {
                        CircularProgressIndicator()
                    }
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

        if (displaySyncFailedDialog) {
            PushNotificationSyncFailedDialog {
                displaySyncFailedDialog = false
            }
        }
    }
}