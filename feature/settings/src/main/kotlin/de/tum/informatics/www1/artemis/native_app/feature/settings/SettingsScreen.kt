package de.tum.informatics.www1.artemis.native_app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.unsubscribeFromNotifications
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
private data object SettingsScreen

@Serializable
private data object PushNotificationSettingsScreen

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit) {
    navigate(SettingsScreen, builder)
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
    onDisplayThirdPartyLicenses: () -> Unit
) {
    composable<SettingsScreen> {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            versionCode = versionCode,
            versionName = versionName,
            onNavigateUp = onNavigateUp,
            onLoggedOut = onLoggedOut,
            onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses
        ) {
            navController.navigate(PushNotificationSettingsScreen)
        }
    }

    composable<PushNotificationSettingsScreen> {
        PushNotificationSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = onNavigateUp
        )
    }
}

/**
 * Display the settings screen.
 * Contains account settings, push settings and general info such as imprint and privacy policy.
 */
@Composable
private fun SettingsScreen(
    modifier: Modifier,
    versionCode: Int,
    versionName: String,
    onNavigateUp: () -> Unit,
    onLoggedOut: () -> Unit,
    onDisplayThirdPartyLicenses: () -> Unit,
    onRequestOpenNotificationSettings: () -> Unit
) {
    val linkOpener = LocalLinkOpener.current

    val pushNotificationJobService: PushNotificationJobService = koinInject()
    val pushNotificationConfigurationService: PushNotificationConfigurationService = koinInject()

    val accountService: AccountService = koinInject()
    val authenticationData: AccountService.AuthenticationData? by accountService.authenticationData.collectAsState(
        initial = null
    )
    val serverConfigurationService: ServerConfigurationService = koinInject()
    val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")

    val scope = rememberCoroutineScope()

    // The auth token if logged in or null otherwise
    val authToken: String? =
        (authenticationData as? AccountService.AuthenticationData.LoggedIn)?.authToken
    val hasUserSelectedInstance by serverConfigurationService.hasUserSelectedInstance.collectAsState(
        initial = false
    )

    val username = when (val authData = authenticationData) {
        is AccountService.AuthenticationData.LoggedIn -> authData.username
        else -> null
    }

    val accountDataService: AccountDataService = koinInject()
    val networkStatusProvider: NetworkStatusProvider = koinInject()

    val accountDataFlow: StateFlow<DataState<Account>?> = remember {
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authenticationData
        ) { serverUrl, authData ->
            when (authData) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                        accountDataService.getAccountData(serverUrl, authData.authToken)
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = padding.calculateTopPadding())
                .padding(
                    bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                ),
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
                            unsubscribeFromNotifications(
                                serverConfigurationService,
                                accountService,
                                pushNotificationConfigurationService,
                                pushNotificationJobService
                            )

                            accountService.logout()

                            onLoggedOut()
                        }
                    }
                )
            }

            NotificationSection(
                modifier = Modifier.fillMaxWidth(),
                onOpenNotificationSettings = onRequestOpenNotificationSettings
            )

            AboutSection(
                modifier = Modifier.fillMaxWidth(),
                hasUserSelectedInstance = hasUserSelectedInstance,
                serverUrl = serverUrl,
                onOpenPrivacyPolicy = {
                    val link = URLBuilder(serverUrl).appendPathSegments("privacy").buildString()

                    linkOpener.openLink(link)
                },
                onOpenImprint = {
                    val link = URLBuilder(serverUrl).appendPathSegments("imprint").buildString()
                    linkOpener.openLink(link)
                },
                onOpenThirdPartyLicenses = onDisplayThirdPartyLicenses,
                // it can only be unselected, if the user has navigated to the settings from the instance selection screen.
                // Therefore, a simple navigate up will let the user select the server instance.
                onRequestSelectServerInstance = onNavigateUp
            )

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
                    icon = Icons.Default.Person,
                    text = stringResource(
                        id = R.string.settings_account_information_full_name,
                    ),
                    valueText = account.name.orEmpty(),
                    onClick = {}
                )

                PreferenceEntry(
                    modifier = childModifier,
                    icon = Icons.Filled.AlternateEmail,
                    text = stringResource(
                        id = R.string.settings_account_information_login,
                    ),
                    valueText = username,
                    onClick = {}
                )

                PreferenceEntry(
                    modifier = childModifier,
                    icon = Icons.Default.Mail,
                    text = stringResource(
                        id = R.string.settings_account_information_email
                    ),
                    valueText = account.email.orEmpty(),
                    onClick = {}
                )
            }
        }

        ButtonEntry(
            modifier = childModifier,
            text = stringResource(id = R.string.settings_account_logout),
            isFocused = true,
            textColor = MaterialTheme.colorScheme.error,
            onClick = onRequestLogout
        )
    }
}

@Composable
private fun NotificationSection(modifier: Modifier, onOpenNotificationSettings: () -> Unit) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_notification_section)
    ) {
        ButtonEntry(
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
    serverUrl: String,
    onRequestSelectServerInstance: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenImprint: () -> Unit,
    onOpenThirdPartyLicenses: () -> Unit
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_about_section)
    ) {
        val linkOpener = LocalLinkOpener.current

        if (!BuildConfig.hasInstanceRestriction) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(id = R.string.settings_server_specifics_information),
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (hasUserSelectedInstance) {
            ServerURLEntry(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_server_url),
                valueText = serverUrl,
                onClick = {
                    val builder = URLBuilder(serverUrl).buildString()
                    linkOpener.openLink(builder)
                }
            )

            ButtonEntry(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.settings_about_privacy_policy),
                onClick = onOpenPrivacyPolicy
            )

            ButtonEntry(
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

        ButtonEntry(
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
            text = stringResource(id = R.string.settings_build_version_code),
            valueText = versionCode.toString(),
            onClick = {}
        )

        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_build_version_name),
            valueText = versionName,
            onClick = { }
        )
    }
}

@Composable
fun PreferenceEntry(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    valueText: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        valueText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

        }
    }
}

@Composable
fun ServerURLEntry(
    modifier: Modifier = Modifier,
    text: String,
    valueText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ButtonEntry(
    modifier: Modifier = Modifier,
    text: String,
    isFocused: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = if (isFocused) Arrangement.Center else Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = if (isFocused) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge
        )

        if (!isFocused) {
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun PreferenceSection(
    modifier: Modifier,
    title: String,
    entries: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            entries()
        }
    }
}