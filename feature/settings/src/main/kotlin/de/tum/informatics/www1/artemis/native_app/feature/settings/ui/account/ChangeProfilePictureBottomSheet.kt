package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account

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
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BottomSheetActionButton
import de.tum.informatics.www1.artemis.native_app.feature.settings.R


@Composable
internal fun ChangeProfilePictureBottomSheet(
    accountDataState: DataState<Account>,
    onOpenFilePicker: () -> Unit,
    onDeleteProfilePicture: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                onClick = onOpenFilePicker
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