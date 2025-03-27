package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsScreen
import kotlinx.serialization.Serializable


@Serializable
private data object Settings

@Serializable
private data object SettingsScreen

@Serializable
private data object PushNotificationSettingsScreen

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit) {
    navigate(Settings, builder)
}

fun NavGraphBuilder.settingsNavGraph(
    navController: NavController,
    onDisplayThirdPartyLicenses: () -> Unit
) {
    navigation<Settings>(
        startDestination = SettingsScreen,
    ) {
        animatedComposable<SettingsScreen>(
            exitTransition = {
                val toLoginScreen = targetState.destination.route?.startsWith(LoginScreen::class.qualifiedName!!) ?: false
                if (toLoginScreen) {
                    return@animatedComposable DefaultTransition.fadeOut
                }
                DefaultTransition.exit
            }
        ) {
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses
            ) {
                navController.navigate(PushNotificationSettingsScreen)
            }
        }

        animatedComposable<PushNotificationSettingsScreen> {
            PushNotificationSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onDone = navController::navigateUp
            )
        }
    }
}