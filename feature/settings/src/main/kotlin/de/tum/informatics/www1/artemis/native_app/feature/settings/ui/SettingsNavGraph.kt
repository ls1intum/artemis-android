package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.login.LoginScreenRoute
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account.AccountSettingsScreen
import kotlinx.serialization.Serializable

@Serializable
private object SettingsSceenRoute {
    @Serializable
    data object Main

    @Serializable
    data object AccountDetails

    @Serializable
    data object PushNotification
}

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit) {
    navigate(SettingsSceenRoute, builder)
}

fun NavGraphBuilder.settingsNavGraph(
    navController: NavController,
    onDisplayThirdPartyLicenses: () -> Unit
) {
    navigation<SettingsSceenRoute>(
        startDestination = SettingsSceenRoute.Main,
    ) {
        animatedComposable<SettingsSceenRoute.Main>(
            exitTransition = {
                val toLoginScreen = targetState.destination.route?.startsWith(LoginScreenRoute::class.qualifiedName!!) ?: false
                if (toLoginScreen) {
                    return@animatedComposable DefaultTransition.fadeOut
                }
                DefaultTransition.exit
            }
        ) {
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses,
                onRequestOpenAccountSettings = {
                    navController.navigate(SettingsSceenRoute.AccountDetails)
                },
                onRequestOpenNotificationSettings = {
                    navController.navigate(SettingsSceenRoute.PushNotification)
                }
            )
        }

        animatedComposable<SettingsSceenRoute.PushNotification> {
            PushNotificationSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onDone = navController::navigateUp
            )
        }

        animatedComposable<SettingsSceenRoute.AccountDetails> {
            AccountSettingsScreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}