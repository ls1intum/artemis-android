package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import org.koin.compose.koinInject


@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject(),
    onNavigateUp: () -> Unit,
) {

}


@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    accountDataState: DataState<Account>,
    onLogout: () -> Unit,
    onRequestReload: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                navigationIcon = {
                    NavigationBackButton(onNavigateUp)
                },
                title = {
                    Text(text = stringResource(id = R.string.account_settings_title))
                }
            )
        },
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier.padding(padding),
            dataState = accountDataState,
            onClickRetry = onRequestReload
        ) { account ->
            AccountSettingsBody(
                modifier = Modifier.fillMaxWidth(),
                account = account,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun AccountSettingsBody(
    modifier: Modifier = Modifier,
    account: Account,
    onLogout: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .pagePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfilePicture(
            modifier = Modifier.size(92.dp),
            profilePictureData = ProfilePictureData.fromAccount(account)
        )

        PreferenceSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.account_settings_section),
        ) {
            val childModifier = Modifier.fillMaxWidth()

            PreferenceEntry(
                modifier = childModifier,
                icon = Icons.Default.AssignmentInd,
                text = stringResource(
                    id = R.string.settings_account_information_full_name,
                ),
                valueText = account.name,
                onClick = {}
            )

            PreferenceEntry(
                modifier = childModifier,
                icon = Icons.Filled.AlternateEmail,
                text = stringResource(
                    id = R.string.settings_account_information_login,
                ),
                valueText = account.username,
                onClick = {}
            )

            PreferenceEntry(
                modifier = childModifier,
                icon = Icons.Default.Mail,
                text = stringResource(
                    id = R.string.settings_account_information_email
                ),
                valueText = account.email,
                onClick = {}
            )

            LogoutButtonEntry(
                modifier = childModifier,
                onRequestLogout = onLogout
            )
        }
    }

}