package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject


@Serializable
data class LoginScreen(val nextDestination: String?)

/**
 * @param nextDestination the deep link to a destination that should be opened after a successful login
 */
fun NavController.navigateToLogin(
    nextDestination: String? = null,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(LoginScreen(nextDestination), builder)
}

/**
 * Switch between actual login and notification configuration.
 */
fun NavGraphBuilder.loginScreen(
    onFinishedLoginFlow: (deepLink: String?) -> Unit,
    onRequestOpenSettings: () -> Unit
) {
    animatedComposable<LoginScreen>(
        enterTransition = { DefaultTransition.fadeIn },
    ) {
        val screen = it.toRoute<LoginScreen>()
        val nextDestinationValue = screen.nextDestination

        var nextDestination by remember(nextDestinationValue) {
            mutableStateOf(if (nextDestinationValue == null || nextDestinationValue == "null") null else nextDestinationValue)
        }

        val scope = rememberCoroutineScope()
        val serverNotificationStorageService: ServerNotificationStorageService = koinInject()
        val serverConfigurationService: ServerConfigurationService = koinInject()

        var currentContent by rememberSaveable { mutableStateOf(LoginScreenContent.LOGIN) }

        val onFinishedLoginFlowImpl = {
            onFinishedLoginFlow(nextDestination)
        }

        AnimatedContent(
            targetState = currentContent,
            transitionSpec = {
                DefaultTransition.navigateForward
            },
            label = "Login <-> Notification configuration"
        ) { content ->
            when (content) {
                LoginScreenContent.LOGIN -> {
                    LoginUiScreen(
                        modifier = Modifier.fillMaxSize(),
                        onLoggedIn = {
                            // Only display notification settings on the first login for the server
                            scope.launch {
                                val serverUrl = serverConfigurationService.serverUrl.first()
                                if (serverNotificationStorageService.hasDisplayedForServer(serverUrl)) {
                                    onFinishedLoginFlowImpl()
                                } else {
                                    currentContent = LoginScreenContent.NOTIFICATION_SETTINGS
                                }
                            }
                        },
                        onRequestOpenSettings = onRequestOpenSettings,
                        onNavigatedToInstanceSelection = {
                            nextDestination = null
                        }
                    )
                }

                LoginScreenContent.NOTIFICATION_SETTINGS -> {
                    PushNotificationSettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        isInitialNotificationSettingsScreen = true,
                        onDone = {
                            scope.launch {
                                serverNotificationStorageService.setHasDisplayed(
                                    serverConfigurationService.serverUrl.first()
                                )
                                onFinishedLoginFlowImpl()
                            }
                        }
                    )
                }
            }
        }
    }
}

private enum class LoginScreenContent {
    LOGIN,
    NOTIFICATION_SETTINGS
}