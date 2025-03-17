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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.LogoutButtonEntry
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.PreferenceEntry
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.PreferenceSection
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.SettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.ProfilePictureUploadResult
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util.getMessage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject(),
    onNavigateUp: () -> Unit,
) {
    val account by viewModel.account.collectAsState()
    val context = LocalContext.current

    AccountSettingsScreen(
        modifier = modifier,
        accountDataState = account,
        onDeleteProfilePicture = viewModel::onDeleteProfilePicture,
        onUploadProfilePicture = viewModel::onUploadProfilePicture,
        onLogout = viewModel::onRequestLogout,
        onRequestReload = viewModel::requestReload,
        onNavigateUp = onNavigateUp
    )
}


@Composable
internal fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    accountDataState: DataState<Account>,
    onDeleteProfilePicture: () -> Unit,
    onUploadProfilePicture: (ImageBitmap) -> Deferred<ProfilePictureUploadResult>,
    onLogout: () -> Unit,
    onRequestReload: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    var showChangeActionsBottomSheet by remember { mutableStateOf(false) }
    var uploadJob: Deferred<ProfilePictureUploadResult>? by remember { mutableStateOf(null) }

    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            val result = imageCropper.crop(
                uri = uri,
                context = context,
            )

            when (result) {
                CropResult.Cancelled -> { }
                is CropError -> { }
                is CropResult.Success -> {
                    uploadJob = onUploadProfilePicture(result.bitmap)
                }
            }
        }
    })

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

    val cropState = imageCropper.cropState
    LaunchedEffect(cropState) {
        if (cropState == null) return@LaunchedEffect
        val initialRectSideLength = kotlin.math.min(cropState.region.width, cropState.region.height)
        cropState.region = Rect(Offset.Zero, Size(initialRectSideLength, initialRectSideLength))
        cropState.aspectLock = true
    }

    fun launchImagePicker() {
        imagePicker.pick()
    }

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
                onChangeClicked = {
                    if (account.hasCustomProfilePicture) {
                        showChangeActionsBottomSheet = true
                    } else {
                        launchImagePicker()
                    }
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

        if(cropState != null) {
            ImageCropperDialog(
                state = cropState,
                style = CropperStyle(
                    autoZoom = false
                ),
                topBar = {

                },
                cropControls = {
                    Button(
                        onClick = {
                            it.done(true)
                        }
                    ) {
                        Text("Crop")
                    }
                }
            )
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Icons.Default.PhotoCamera
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