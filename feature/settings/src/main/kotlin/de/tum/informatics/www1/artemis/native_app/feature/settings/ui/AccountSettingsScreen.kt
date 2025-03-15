package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BottomSheetActionButton
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
    val account by viewModel.account.collectAsState()

    AccountSettingsScreen(
        modifier = modifier,
        accountDataState = account,
        onLogout = viewModel::onRequestLogout,
        onRequestReload = viewModel::requestReload,
        onNavigateUp = onNavigateUp
    )
}


@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    accountDataState: DataState<Account>,
    onLogout: () -> Unit,
    onRequestReload: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    var showChangeActionsBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
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
                onChangeClicked = { showChangeActionsBottomSheet = true },
                onLogout = onLogout
            )
        }

        if (showChangeActionsBottomSheet) {
            ModalBottomSheet(
                modifier = Modifier
                    .statusBarsPadding(),
                contentWindowInsets = { WindowInsets.navigationBars },
                onDismissRequest = { showChangeActionsBottomSheet = false },
            ) {
                val hasCustomProfilePicture = accountDataState.orNull()?.hasCustomProfilePicture ?: false

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacings.BottomSheetContentPadding)
                ) {
                    BottomSheetActionButton(
                        icon = Icons.Default.PhotoLibrary,
                        text = stringResource(R.string.account_settings_change_profile_picture_from_library),
                        onClick = {
                            showChangeActionsBottomSheet = false
                        }
                    )

                    if (hasCustomProfilePicture) {
                        BottomSheetActionButton(
                            icon = Icons.Default.Delete,
                            text = stringResource(R.string.account_settings_delete_profile_picture),
                            onClick = {
                                showChangeActionsBottomSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSettingsBody(
    modifier: Modifier = Modifier,
    account: Account,
    onChangeClicked: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .pagePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChangeProfilePicture(
            account = account,
            onChangeClicked = onChangeClicked
        )

        DetailsSection(
            account = account,
        )

        LogoutButtonEntry(
            modifier = Modifier.fillMaxWidth(),
            onRequestLogout = onLogout
        )
    }

}

@Composable
private fun ChangeProfilePicture(
    account: Account,
    onChangeClicked: () -> Unit
) {
    Column {
        ProfilePicture(
            modifier = Modifier.size(200.dp),
            profilePictureData = ProfilePictureData.fromAccount(account)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ChangeProfilePictureButton(
            hasCustomProfilePicture = account.hasCustomProfilePicture,
            onChangeClicked = onChangeClicked,
        )
    }
}

@Composable
private fun ChangeProfilePictureButton(
    modifier: Modifier = Modifier,
    hasCustomProfilePicture: Boolean,
    onChangeClicked: () -> Unit,
) {
    FilledTonalButton(
        modifier = modifier,
        onClick = onChangeClicked
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val icon = if (hasCustomProfilePicture) {
                Icons.Default.Edit
            } else {
                Icons.Default.Camera
            }

            Icon(
                imageVector = icon,
                contentDescription = null
            )

            val text = if (hasCustomProfilePicture) {
                stringResource(R.string.account_settings_edit_profile_picture)
            } else {
                stringResource(R.string.account_settings_upload_profile_picture)
            }

            Text(text)
        }
    }
}

@Composable
private fun DetailsSection(
    account: Account,
) {
    val childModifier = Modifier.fillMaxWidth()

    PreferenceSection(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.account_settings_section),
    ) {

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
    }
}