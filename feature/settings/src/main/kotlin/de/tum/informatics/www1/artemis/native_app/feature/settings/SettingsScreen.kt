package de.tum.informatics.www1.artemis.native_app.feature.settings

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

private const val SETTINGS_DESTINATION = "settings"

fun NavController.navigateToSettings(builder: NavOptionsBuilder.() -> Unit) {
    navigate(SETTINGS_DESTINATION, builder)
}

/**
 * version information has to be supplied externally, as they come from the app module
 */
fun NavGraphBuilder.settingsScreen(
    versionCode: Int,
    versionName: String,
    onNavigateUp: () -> Unit,
    onLoggedOut: () -> Unit,
    onDisplayThirdPartyLicenses: () -> Unit
) {
    composable(SETTINGS_DESTINATION) {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            versionCode = versionCode,
            versionName = versionName,
            onLoggedOut = onLoggedOut,
            onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses,
            onNavigateUp = onNavigateUp
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
    onDisplayThirdPartyLicenses: () -> Unit
) {
    val accountService: AccountService = get()
    val authenticationData: AccountService.AuthenticationData? by accountService.authenticationData.collectAsState(
        initial = null
    )
    val serverConfigurationService: ServerConfigurationService = get()
    val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")


    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
            UserInformationSection(
                modifier = Modifier.fillMaxWidth(),
                authData = authenticationData,
                onRequestLogout = {
                    scope.launch {
                        accountService.logout()
                        onLoggedOut()
                    }
                }
            )

            Divider()

            NotificationSection(
                modifier = Modifier.fillMaxWidth(),
                onOpenNotificationSettings = {}
            )

            Divider()

            AboutSection(
                modifier = Modifier.fillMaxWidth(),
                onOpenPrivacyPolicy = {
                    val link = URLBuilder(serverUrl).appendPathSegments("privacy").buildString()

                    openLink(context, link)
                },
                onOpenThirdPartyLicenses = onDisplayThirdPartyLicenses
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
    authData: AccountService.AuthenticationData?,
    onRequestLogout: () -> Unit
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_account_information_section)
    ) {
        val childModifier = Modifier.fillMaxWidth()

        if (authData is AccountService.AuthenticationData.LoggedIn) {
            EmptyDataStateUi(
                dataState = authData.account,
                otherwise = {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
                        account.username.orEmpty()
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
    onOpenPrivacyPolicy: () -> Unit,
    onOpenThirdPartyLicenses: () -> Unit
) {
    PreferenceSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_about_section)
    ) {
        PreferenceEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_about_privacy_policy),
            onClick = onOpenPrivacyPolicy
        )

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

private fun openLink(context: Context, link: String) {
    val customTabs = CustomTabsIntent.Builder().build()
    customTabs.launchUrl(context, Uri.parse(link))
}