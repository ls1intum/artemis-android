package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BottomSheetActionButton
import de.tum.informatics.www1.artemis.native_app.feature.settings.R


private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")

@Composable
internal fun ChangeProfilePictureBottomSheet(
    accountDataState: DataState<Account>,
    onFileSelected: (Uri) -> Unit,
    onDeleteProfilePicture: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberFilePickerLauncher(
        context = context,
        onFileSelected = {
            onFileSelected(it)
            onDismiss()
        }
    )

    ModalBottomSheet(
        modifier = Modifier
            .statusBarsPadding(),
        contentWindowInsets = { WindowInsets.navigationBars },
        onDismissRequest = onDismiss,
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
                    filePickerLauncher.launch("image/*")
                }
            )

            if (hasCustomProfilePicture) {
                BottomSheetActionButton(
                    icon = Icons.Default.Delete,
                    text = stringResource(R.string.account_settings_delete_profile_picture),
                    onClick = {
                        onDeleteProfilePicture()
                        onDismiss()
                    }
                )
            }
        }
    }
}


@Composable
private fun rememberFilePickerLauncher(
    context: Context,
    onFileSelected: (Uri) -> Unit
) = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult

    val mimeType = context.contentResolver.getType(uri)
    if (mimeType in ALLOWED_MIME_TYPES) {
        onFileSelected(uri)
    } else {
        Toast.makeText(
            context,
            "This file type is not supported for profile pictures",
            Toast.LENGTH_SHORT
        ).show()
    }
}