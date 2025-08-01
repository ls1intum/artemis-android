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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.push.R
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

    val onNavigateBack: () -> Unit = {
        onDone()
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
        } ,
        floatingActionButton = {
            if (isInitialNotificationSettingsScreen) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onDone()
                    },
                    text = {
                        Text(
                            text = stringResource(
                                id = R.string.initial_push_notification_settings_fab_text_without_save
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                )
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
    }
}