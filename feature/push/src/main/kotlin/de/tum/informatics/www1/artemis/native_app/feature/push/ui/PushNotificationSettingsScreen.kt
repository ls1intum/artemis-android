package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Displays the notification settings screen.
 * Contains PushNotificationSettingsUi and is used in the settings screen and after login.
 */
@Composable
fun PushNotificationSettingsScreen(
    modifier: Modifier = Modifier,
    isInitialNotificationSettingsScreen: Boolean = false,
    onDone: () -> Unit
) {
    val viewModel: PushNotificationSettingsViewModel = koinViewModel()
    val isDirty by viewModel.isDirty.collectAsState(initial = false)

    var saveJob: Job? by remember { mutableStateOf(null) }
    var displaySyncFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }
    var displayUnsavedChangesDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val onNavigateBack: () -> Unit = {
        if (isDirty) {
            displayUnsavedChangesDialog = true
        } else {
            onDone()
        }
    }
    val saveChanges: () -> Unit = {
        saveJob = viewModel.saveSettings()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val isSuccessful = (saveJob as Deferred<Boolean>).await()
                if (isSuccessful) {
                    onDone()
                } else {
                    displayUnsavedChangesDialog = false
                    displaySyncFailedDialog = true
                }
            } catch (e: Exception) {
                displayUnsavedChangesDialog = false
                displaySyncFailedDialog = true
            }
        }
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (isInitialNotificationSettingsScreen) return@TopAppBar

                    NavigationBackButton(onNavigateBack = {
                        onNavigateBack()
                    })
                },
                title = {
                    if (isInitialNotificationSettingsScreen) {
                        Text(text = stringResource(id = R.string.initial_push_notification_settings_title))
                    } else {
                        Text(text = stringResource(id = R.string.settings_push_notification_settings_screen_title))
                    }
                }
            )
        },
        floatingActionButton = {
            if (isInitialNotificationSettingsScreen) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // If changes have been made, these need to be synced first.
                        if (isDirty) saveChanges() else onDone()
                    },
                    text = {
                        Text(
                            text = stringResource(
                                id =
                                if (isDirty) R.string.initial_push_notification_settings_fab_text_with_save
                                else R.string.initial_push_notification_settings_fab_text_without_save
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = if (isDirty) Icons.Default.Save else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                )
            } else {
                JobAnimatedFloatingActionButton(
                    enabled = isDirty,
                    startJob = { viewModel.saveSettings() },
                    onJobCompleted = { isSuccessful ->
                        if (!isSuccessful) {
                            displaySyncFailedDialog = true
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
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(
                    bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
            viewModel = viewModel
        )

        if (displaySyncFailedDialog) {
            PushNotificationSyncFailedDialog {
                displaySyncFailedDialog = false
            }
        }

        if (displayUnsavedChangesDialog) {
            PushNotificationUnsavedChangesDialog(
                onDismissRequest = {
                    displayUnsavedChangesDialog = false
                    onDone()
                },
                onSaveChanges = saveChanges
            )
        }

        if (saveJob != null && isInitialNotificationSettingsScreen) {
            PushNotificationSyncChangesDialog(
                onDismissRequest = {
                    saveJob?.cancel()
                    saveJob = null
                }
            )
        }
    }
}