package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.ForwardedMessagePreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ForwardMessageReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import kotlinx.coroutines.CompletableDeferred

@Composable
fun PostForwardBottomSheet(
    post: IBasePost,
    forwardMessageUseCase: ForwardMessageUseCase,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .pagePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.post_forward),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { },
                    modifier = Modifier
                ) {
                    Text(
                        text = stringResource(R.string.post_forward_send),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            MemberSelection(
                modifier = Modifier.fillMaxWidth(),
                viewModel = forwardMessageUseCase,
                isConversationSelectionEnabled = true,
                memberSelectionMode = MemberSelectionMode.MemberSelectionDropdown,
                onUpdateSelectedUserCount = { }
            )

            HorizontalDivider()

            ForwardMessageReplyTextField(
                modifier = Modifier.fillMaxWidth(),
                initialReplyTextProvider = forwardMessageUseCase,
                hintText = stringResource(R.string.post_forward_hint),
                onFileSelected = { uri, ->
                    forwardMessageUseCase.onFileSelected(uri, context)
                },
                sendButton = { },
                onCreateForwardedMessage = { CompletableDeferred() },
                textOptionsTopContent = {
                    ForwardedMessagePreview(
                        modifier = Modifier.fillMaxWidth(),
                        forwardedPost = post
                    )
                }
            )
        }
    }
}