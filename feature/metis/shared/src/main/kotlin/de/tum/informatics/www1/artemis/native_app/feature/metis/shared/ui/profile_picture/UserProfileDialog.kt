package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.UserRoleIcon


@Composable
fun UserProfileDialog(
    username: String,
    userRole: UserRole?,
    profilePictureData: ProfilePictureData,
    isAppUser: Boolean,
    onDismiss: () -> Unit,
    onSendMessageClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            UserProfileDialogHeader(
                username = username,
                userRole = userRole,
                profilePictureData = profilePictureData,
            )
        },
        text = {
            if (!isAppUser) {
                FilledTonalButton(
                    onClick = onSendMessageClick,
                ) {
                    Text(stringResource(R.string.user_profile_dialog_send_message_action))
                }
            } else {
                Text(
                    stringResource(R.string.user_profile_dialog_is_app_user_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(R.string.user_profile_dialog_close))
            }
        },
    )

}

@Composable
private fun UserProfileDialogHeader(
    username: String,
    userRole: UserRole?,
    profilePictureData: ProfilePictureData,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ProfilePicture(
            modifier = Modifier.size(80.dp),
            profilePictureData = profilePictureData,
        )
        
        Column(
            modifier = Modifier.padding(start = 16.dp),
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleMedium,
            )

            userRole?.let {
                 UserRoleRow(userRole = it)
            }
        }
    }
}

@Composable
private fun UserRoleRow(userRole: UserRole) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserRoleIcon(
            modifier = Modifier
                .size(30.dp)
                .padding(end = 4.dp),
            userRole = userRole,
        )

        Text(
            text = userRole.toString(),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}


@Preview
@Composable
fun UserProfileDialogPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        UserProfileDialog(
            username = "Max Mustermann",
            userRole = UserRole.TUTOR,
            profilePictureData = ProfilePictureData.create(
                userId = 1L,
                username = "Max Mustermann",
                imageUrl = null,
            ),
            onDismiss = {},
            isAppUser = true,
            onSendMessageClick = {},
        )
    }
}