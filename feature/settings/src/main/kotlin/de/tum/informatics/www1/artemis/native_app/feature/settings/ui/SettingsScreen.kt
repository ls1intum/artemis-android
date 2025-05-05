package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AnimatedDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import de.tum.informatics.www1.artemis.native_app.feature.settings.BuildConfig
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import org.koin.compose.koinInject


private const val GITHUB_ISSUES_LINK = "https://github.com/ls1intum/artemis-android/issues"

/**
 * Display the settings screen.
 * Contains account settings, push settings and general info such as imprint and privacy policy.
 */
@Composable
internal fun SettingsScreen(
    modifier: Modifier,
    viewModel: SettingsViewModel = koinInject(),
    onDisplayThirdPartyLicenses: () -> Unit,
    onRequestOpenAccountSettings: () -> Unit,
    onRequestOpenNotificationSettings: () -> Unit
) {
    val linkOpener = LocalLinkOpener.current
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val accountData: DataState<Account> by viewModel.account.collectAsState()

    SettingsScreen(
        modifier = modifier,
        accountDataState = accountData,
        isLoggedIn = isLoggedIn,
        serverUrl = artemisContext.serverUrl,
        appVersion = viewModel.appVersion,
        linkOpener = linkOpener,
        onRequestLogout = viewModel::onRequestLogout,
        onRequestOpenAccountSettings = onRequestOpenAccountSettings,
        onRequestOpenNotificationSettings = onRequestOpenNotificationSettings,
        onDisplayThirdPartyLicenses = onDisplayThirdPartyLicenses
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    accountDataState: DataState<Account>,
    isLoggedIn: Boolean,
    serverUrl: String,
    appVersion: AppVersion,
    linkOpener: LinkOpener,
    onRequestLogout: () -> Unit,
    onRequestOpenAccountSettings: () -> Unit,
    onRequestOpenNotificationSettings: () -> Unit,
    onDisplayThirdPartyLicenses: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.settings_screen_title))
                },
                navigationIcon = { NavigationBackButton() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .consumeWindowInsets(WindowInsets.systemBars)
                .pagePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoggedIn) {
                UserInformationSection(
                    modifier = Modifier.fillMaxWidth(),
                    accountDataState = accountDataState,
                    onRequestLogout = onRequestLogout,
                    onNavigateToAccountSettings = onRequestOpenAccountSettings
                )

                NotificationSection(
                    modifier = Modifier.fillMaxWidth(),
                    onOpenNotificationSettings = onRequestOpenNotificationSettings
                )
            }

            AboutSection(
                modifier = Modifier.fillMaxWidth(),
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
            )

            BuildInformationSection(
                modifier = Modifier.fillMaxWidth(),
                versionCode = appVersion.versionCode,
                versionName = appVersion.fullVersionName,
                onReportBug = {
                    linkOpener.openLink(GITHUB_ISSUES_LINK)
                }
            )
        }
    }
}

@Composable
private fun UserInformationSection(
    modifier: Modifier,
    accountDataState: DataState<Account>,
    onRequestLogout: () -> Unit,
    onNavigateToAccountSettings: () -> Unit
) {
    ArtemisSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_account_information_section)
    ) {
        AnimatedDataStateUi(
            dataState = accountDataState,
            failureContent = {
                LogoutButtonEntry(
                    modifier = Modifier.fillMaxWidth(),
                    onRequestLogout = onRequestLogout
                )
            }
        ) { account ->
            ButtonEntry(
                onClick = onNavigateToAccountSettings
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePicture(
                        modifier = Modifier.size(52.dp),
                        profilePictureData = ProfilePictureData.fromAccount(account)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = account.name.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = account.username.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSection(modifier: Modifier, onOpenNotificationSettings: () -> Unit) {
    ArtemisSection (
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
    serverUrl: String,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenImprint: () -> Unit,
    onOpenThirdPartyLicenses: () -> Unit
) {
    ArtemisSection(
        modifier = modifier,
        title = stringResource(id = R.string.settings_about_section),
        description = if (!BuildConfig.hasInstanceRestriction) stringResource(id = R.string.settings_server_specifics_information) else null
    ) {
        val linkOpener = LocalLinkOpener.current

        if (serverUrl.isNotEmpty()) {
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
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.settings_server_specifics_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
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
    onReportBug: () -> Unit,
) {
    ArtemisSection(
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

        ButtonEntry(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.settings_report_bug),
            textColor = MaterialTheme.colorScheme.error,
            isFocused = true,
            onClick = onReportBug
        )
    }
}

@Composable
private fun ServerURLEntry(
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
