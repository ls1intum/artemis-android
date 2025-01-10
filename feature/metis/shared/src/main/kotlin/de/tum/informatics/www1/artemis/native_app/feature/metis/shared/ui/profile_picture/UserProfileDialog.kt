package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.UserRoleIcon
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.LocalVisibleMetisContextManager
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


const val TEST_TAG_USER_PROFILE_DIALOG = "TEST_TAG_USER_PROFILE_DIALOG"


@Composable
fun UserProfileDialog(
    modifier: Modifier = Modifier,
    userId: Long,
    username: String,
    userRole: UserRole?,
    profilePictureData: ProfilePictureData,
    onDismiss: () -> Unit,
) {
    val metisContext= LocalVisibleMetisContextManager.current.getMostRecentMetisContext()
    require(metisContext != null) { "No MetisContext provided." }

    val courseId = metisContext.metisContext.courseId

    val viewModel = koinViewModel<UserProfileDialogViewModel>(
        key = "$courseId|$userId",
    ) {
        parametersOf(courseId, userId)
    }

    val isSendMessageAvailable by viewModel.isSendMessageAvailable.collectAsState()
    val context = LocalContext.current

    UserProfileDialogImpl(
        modifier = modifier,
        username = username,
        userRole = userRole,
        profilePictureData = profilePictureData,
        isSendMessageAvailable = isSendMessageAvailable,
        onSendMessageClick = {
            viewModel.navigateToOneToOneChat(context)
            onDismiss()
        },
        onDismiss = onDismiss
    )
}


@Composable
private fun UserProfileDialogImpl(
    modifier: Modifier,
    username: String,
    userRole: UserRole?,
    profilePictureData: ProfilePictureData,
    isSendMessageAvailable: Boolean,
    onSendMessageClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier.testTag(TEST_TAG_USER_PROFILE_DIALOG),
        onDismissRequest = onDismiss,
        title = {
            UserProfileDialogHeader(
                username = username,
                userRole = userRole,
                profilePictureData = profilePictureData,
            )
        },
        text = {
            if (isSendMessageAvailable) {
                Button(
                    onClick = onSendMessageClick,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)

                        Text(stringResource(R.string.user_profile_dialog_send_message_action))
                    }
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
        UserProfileDialogImpl(
            modifier = Modifier,
            username = "Max Mustermann",
            userRole = UserRole.TUTOR,
            profilePictureData = ProfilePictureData.create(
                userId = 1L,
                username = "Max Mustermann",
                imageUrl = null,
            ),
            isSendMessageAvailable = true,
            onSendMessageClick = {},
            onDismiss = {},
        )
    }
}