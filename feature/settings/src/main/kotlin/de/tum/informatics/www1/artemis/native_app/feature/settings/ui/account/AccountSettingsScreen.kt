package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.attafitamim.krop.core.crop.rememberImageCropper
import de.tum.informatics.www1.artemis.native_app.core.common.ActiveModuleFeature
import de.tum.informatics.www1.artemis.native_app.core.common.FeatureAvailability
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.LogoutButtonEntry
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.PreferenceEntry
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.SettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys.PasskeysSection
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys.PasskeysUseCase
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.ProfilePictureUploadResult
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.getMessage
import kotlinx.coroutines.Deferred
import org.koin.compose.koinInject

@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject(),
) {
    val account by viewModel.account.collectAsState()
    val passkeysUseCase = viewModel.passkeysUseCase
    val passkeysEnabled = FeatureAvailability.isEnabled(ActiveModuleFeature.Passkey)
    val passkeys: DataState<List<PasskeyDTO>>? = if (passkeysEnabled) {
        passkeysUseCase.passkeys.collectAsState().value
    } else {
        null
    }

    AccountSettingsScreen(
        modifier = modifier,
        accountDataState = account,
        passkeys = passkeys,
        onDeleteProfilePicture = viewModel::onDeleteProfilePicture,
        onUploadProfilePicture = viewModel::onUploadProfilePicture,
        onCreatePasskey = passkeysUseCase::createPasskey,
        onLogout = viewModel::onRequestLogout,
        onRequestReload = viewModel::onRequestReload
    )
}


@Composable
internal fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    accountDataState: DataState<Account>,
    passkeys: DataState<List<PasskeyDTO>>?,
    onDeleteProfilePicture: () -> Unit,
    onUploadProfilePicture: (ImageBitmap) -> Deferred<ProfilePictureUploadResult>,
    onCreatePasskey: () -> Deferred<PasskeysUseCase.CreationResult>,
    onLogout: () -> Unit,
    onRequestReload: () -> Unit
) {
    var showChangeActionsBottomSheet by remember { mutableStateOf(false) }
    var uploadJob: Deferred<ProfilePictureUploadResult>? by remember { mutableStateOf(null) }
    var createPasskeyJob: Deferred<PasskeysUseCase.CreationResult>? by remember { mutableStateOf(null) }
    var createPasskeyResult by remember { mutableStateOf<PasskeysUseCase.CreationResult?>(null) }

    val imageCropper = rememberImageCropper()
    val croppingImagePicker = rememberCroppingImagePicker(
        imageCropper = imageCropper,
        onCropSuccess = {
            uploadJob = onUploadProfilePicture(it)
        }
    )

    val context = LocalContext.current
    AwaitDeferredCompletion(job = uploadJob) {
        uploadJob = null
        showChangeActionsBottomSheet = false

        if (it is ProfilePictureUploadResult.Error) {
            Toast.makeText(
                context,
                it.getMessage(context),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    AwaitDeferredCompletion(job = createPasskeyJob) { result ->
        createPasskeyJob = null
        createPasskeyResult = result
        if (result is PasskeysUseCase.CreationResult.Success) {
            onRequestReload()
        }
    }

    fun launchImagePicker() {
        croppingImagePicker.pick()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationBackButton()
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
                passkeys = passkeys,
                onChangeProfilePicture = {
                    if (account.hasCustomProfilePicture) {
                        showChangeActionsBottomSheet = true
                    } else {
                        launchImagePicker()
                    }
                },
                onCreatePasskey = {
                    createPasskeyJob = onCreatePasskey()
                },
                onLogout = onLogout
            )
        }

        if (showChangeActionsBottomSheet) {
            ChangeProfilePictureBottomSheet(
                accountDataState = accountDataState,
                isLoading = uploadJob != null,
                onOpenFilePicker = {
                    launchImagePicker()
                },
                onDeleteProfilePicture = onDeleteProfilePicture,
                onDismiss = { showChangeActionsBottomSheet = false }
            )
        }

        ImagePickerAndCropper(
            imageCropper = imageCropper
        )

        PasskeyCreationResultHandler(
            result = createPasskeyResult,
            onReset = {
                createPasskeyResult = null
            }
        )
    }
}


@Composable
private fun AccountSettingsBody(
    modifier: Modifier = Modifier,
    account: Account,
    passkeys: DataState<List<PasskeyDTO>>?,  // null means passkeys are disabled
    onChangeProfilePicture: () -> Unit,
    onCreatePasskey: () -> Unit,
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
            onChangeClicked = onChangeProfilePicture
        )

        DetailsSection(
            account = account,
        )

        passkeys?.let {
            PasskeysSection(
                modifier = Modifier.fillMaxWidth(),
                passkeysDataState = it,
                onCreatePasskey = onCreatePasskey
            )
        }

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfilePicture(
            modifier = Modifier.size(160.dp),
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
                Icons.Default.Upload
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

    ArtemisSection(
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

@Composable
private fun PasskeyCreationResultHandler(
    result: PasskeysUseCase.CreationResult?,
    onReset: () -> Unit,
) {
    when (result) {
        is PasskeysUseCase.CreationResult.Success -> {
            Toast.makeText(
                LocalContext.current,
                R.string.passkey_settings_create_success,
                Toast.LENGTH_SHORT
            ).show()
            onReset()
        }

        is PasskeysUseCase.CreationResult.Cancelled -> {
            Toast.makeText(
                LocalContext.current,
                R.string.passkey_settings_create_cancelled,
                Toast.LENGTH_SHORT
            ).show()
            onReset()
        }

        is PasskeysUseCase.CreationResult.Failure -> {
            // Show a dialog
            AlertDialog(
                onDismissRequest = onReset,
                title = {
                    Text(stringResource(R.string.passkey_settings_create_failure_dialog_title))
                },
                text = {
                    Column {
                        Text(stringResource(R.string.passkey_settings_create_failure_dialog_desc))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = onReset
                    ) {
                        Text(stringResource(R.string.passkey_settings_create_failure_dialog_ok))
                    }
                }
            )
        }

        null -> {}
    }
}