package de.tum.informatics.www1.artemis.native_app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

private const val SETTINGS_DESTINATION = "settings"
private const val PUSH_NOTIFICATION_SETTINGS_DESTINATION = "push_notification_settings"

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit) {
    navigate(SETTINGS_DESTINATION, builder)
}

/**
 * version information has to be supplied externally, as they come from the app module
 */
fun NavGraphBuilder.settingsScreen(
    navController: NavController,
    versionCode: Int,
    versionName: String,
    onNavigateUp: () -> Unit,
    onLoggedOut: () -> Unit,
    onDisplayThirdPartyLicenses: () -> Unit,
    onRequestOpenLink: (String) -> Unit
) {
    composable(SETTINGS_DESTINATION) {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            versionCode = versionCode,
            versionName = versionName,
            onLoggedOut = onLoggedOut,
            onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses,
            onNavigateUp = onNavigateUp,
            onRequestOpenLink = onRequestOpenLink,
            onRequestOpenNotificationSettings = {
                navController.navigate(PUSH_NOTIFICATION_SETTINGS_DESTINATION)
            }
        )
    }

    composable(PUSH_NOTIFICATION_SETTINGS_DESTINATION) {
        PushNotificationSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = onNavigateUp
        )
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    versionCode: Int,
    versionName: String,
    onNavigateUp: () -> Unit,
    onLoggedOut: () -> Unit,
    onDisplayThirdPartyLicenses: () -> Unit,
    onRequestOpenLink: (String) -> Unit,
    onRequestOpenNotificationSettings: () -> Unit
) {
    val pushNotificationJobService: PushNotificationJobService = get()

    val accountService: AccountService = get()
    val authenticationData: AccountService.AuthenticationData? by accountService.authenticationData.collectAsState(
        initial = null
    )
    val serverConfigurationService: ServerConfigurationService = get()
    val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")

    val scope = rememberCoroutineScope()

    // The auth token if logged in or null otherwise
    val authToken: String? = (authenticationData as? AccountService.AuthenticationData.LoggedIn)?.authToken
    val hasUserSelectedInstance by serverConfigurationService.hasUserSelectedInstance.collectAsState(
        initial = false
    )

    val username = when (val authData = authenticationData) {
        is AccountService.AuthenticationData.LoggedIn -> authData.username
        else -> null
    }

    val serverDataService: ServerDataService = get()
    val networkStatusProvider: NetworkStatusProvider = get()

    val accountDataFlow: StateFlow<DataState<Account>?> = remember {
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authenticationData
        ) { serverUrl, authData ->
            when (authData) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                        serverDataService.getAccountData(serverUrl, authData.authToken)
                    }
                }

                AccountService.AuthenticationData.NotLoggedIn -> flowOf(null)
            }
        }
            .stateIn(scope, SharingStarted.Eagerly, null)
    }
    val accountData: DataState<Account>? by accountDataFlow.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (authToken != null) {
                UserInformationSection(
                    modifier = Modifier.fillMaxWidth(),
                    authData = accountData,
                    username = username,
                    onRequestLogout = {
                        scope.launch {
                            // the user manually logs out. Therefore we need to tell the server asap.
                            pushNotificationJobService.scheduleUnsubscribeFromNotifications(
                                serverUrl = serverUrl,
                                authToken = authToken
                            )

                            accountService.logout()

                            onLoggedOut()
                        }
                    }
                )

                Divider()

                NotificationSection(
                    modifier = Modifier.fillMaxWidth(),
                    onOpenNotificationSettings = onRequestOpenNotificationSettings
                )

                Divider()
            }

            AboutSection(
                modifier = Modifier.fillMaxWidth(),
                hasUserSelectedInstance = hasUserSelectedInstance,
                onOpenPrivacyPolicy = {
                    val link = URLBuilder(serverUrl).appendPathSegments("privacy").buildString()

                    onRequestOpenLink(link)
                },
                onOpenImprint = {
                    val link = URLBuilder(serverUrl).appendPathSegments("imprint").buildString()
                    onRequestOpenLink(link)
                },
                onOpenThirdPartyLicenses = onDisplayThirdPartyLicenses,
                // it can only be unselected, if the user has navigated to the settings from the instance selection screen.
                // Therefore, a simple navigate up will let the user select the server instance.
                onRequestSelectServerInstance = onNavigateUp
            )

            Divider()

            BuildInformationSection(
                modifier = Modifier.fillMaxWidth(),
                versionCode = versionCode,
                versionName = versionName
            )
        }
    }
}

@Composable
private fun UserInformationSection(
    modifier: Modifier,
    username: String?,
    authData: DataState<Account>?,
    onRequestLogout: () -> Unit
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_account_information_section)
    ) {
        val childModifier = Modifier.fillMaxWidth()

        if (authData != null && username != null) {
            EmptyDataStateUi(
                dataState = authData,
                otherwise = {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            ) { account ->
                PreferenceEntry(
                    modifier = childModifier,
                    text = stringResource(
                        id = R.string.settings_account_information_full_name,
                        account.name.orEmpty()
                    ),
                    onClick = {}
                )

                PreferenceEntry(
                    modifier = childModifier,
                    text = stringResource(
                        id = R.string.settings_account_information_login,
                        username
                    ),
                    onClick = {}
                )

                PreferenceEntry(
                    modifier = childModifier,
                    text = stringResource(
                        id = R.string.settings_account_information_email,
                        account.email.orEmpty()
                    ),
                    onClick = {}
                )

                PreferenceEntry(
                    modifier = childModifier,
                    text = stringResource(id = R.string.settings_account_logout),
                    onClick = onRequestLogout
                )
            }
        }
    }
}

@Composable
private fun NotificationSection(modifier: Modifier, onOpenNotificationSettings: () -> Unit) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_notification_section)
    ) {
        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_notification_settings),
            onClick = onOpenNotificationSettings
        )
    }
}

@Composable
private fun AboutSection(
    modifier: Modifier,
    hasUserSelectedInstance: Boolean,
    onRequestSelectServerInstance: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenImprint: () -> Unit,
    onOpenThirdPartyLicenses: () -> Unit
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_about_section)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.settings_server_specifics_information),
            style = MaterialTheme.typography.labelMedium
        )

        if (hasUserSelectedInstance) {
            PreferenceEntry(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.settings_about_privacy_policy),
                onClick = onOpenPrivacyPolicy
            )

            PreferenceEntry(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.settings_about_imprint),
                onClick = onOpenImprint
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_server_specifics_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = onRequestSelectServerInstance
                ) {
                    Text(text = stringResource(id = R.string.settings_server_specifics_unavailable_select_instance_button))
                }
            }
        }

        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_about_third_party_licences),
            onClick = onOpenThirdPartyLicenses
        )
    }
}

@Composable
private fun BuildInformationSection(
    modifier: Modifier,
    versionCode: Int,
    versionName: String,
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_build_section)
    ) {
        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_build_version_code, versionCode),
            onClick = {}
        )

        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_build_version_name, versionName),
            onClick = { }
        )
    }
}


@Composable
private fun PreferenceSection(
    modifier: Modifier,
    title: String,
    entries: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        val childModifier = Modifier.fillMaxWidth()

        PreferenceSectionTitle(
            modifier = childModifier.padding(horizontal = 16.dp),
            text = title
        )

        entries()
    }
}

@Composable
private fun PreferenceSectionTitle(modifier: Modifier, text: String) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun PreferenceEntry(modifier: Modifier, text: String, onClick: () -> Unit) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
