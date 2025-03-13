package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.ForwardedMessagePreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.ForwardingMessageError
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ForwardMessageReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionMode
import kotlinx.coroutines.CompletableDeferred

// The maximum number of users per group chat is set to 9
private const val MAX_NUMBER_OF_RECIPIENTS = 9

@Composable
fun PostForwardBottomSheet(
    chatListItem: ChatListItem.PostItem,
    forwardMessageUseCase: ForwardMessageUseCase,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val onSendMessage = forwardMessageUseCase::forwardPost
    val post = chatListItem.post
    val postToForward =
        if (chatListItem is ChatListItem.PostItem.ForwardedMessage) chatListItem.forwardedPosts.firstOrNull()
            ?: post else post

    val selectedRecipients = forwardMessageUseCase.recipients.collectAsState()
    val selectedConversations = forwardMessageUseCase.conversations.collectAsState()

    val isMaxRecipientsValueReached = selectedRecipients.value.size > MAX_NUMBER_OF_RECIPIENTS
    val isSendButtonEnabled =
        (selectedRecipients.value.isNotEmpty() || selectedConversations.value.isNotEmpty()) && !isMaxRecipientsValueReached
    val forwardingMessageError by forwardMessageUseCase.forwardingMessageError

    LaunchedEffect(forwardingMessageError) {
        if (forwardingMessageError == ForwardingMessageError.DM_CREATION_ERROR) {
            Toast.makeText(context, R.string.post_forward_error_DM_creation, Toast.LENGTH_SHORT).show()
            forwardMessageUseCase.resetErrorMessage()
        }
        if (forwardingMessageError == ForwardingMessageError.GROUP_CHAT_CREATION_ERROR) {
            Toast.makeText(context, R.string.post_forward_error_group_creation, Toast.LENGTH_SHORT).show()
            forwardMessageUseCase.resetErrorMessage()
        }
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .pagePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
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
                    onClick = {
                        onSendMessage(postToForward) { success ->
                            if (success) {
                                onDismissRequest()
                            }
                        }
                    },
                    enabled = isSendButtonEnabled,
                    modifier = Modifier
                ) {
                    Text(
                        text = stringResource(R.string.post_forward_send),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            MemberSelection(
                modifier = Modifier.fillMaxWidth(),
                viewModel = forwardMessageUseCase,
                isConversationSelectionEnabled = true,
                memberSelectionMode = MemberSelectionMode.MemberSelectionDropdown,
                searchBarBackground = MaterialTheme.colorScheme.surfaceContainerHigh,
                onUpdateSelectedUserCount = { }
            )

            AnimatedVisibility(isMaxRecipientsValueReached) {
                Text(
                    text = stringResource(R.string.post_forward_too_many_recipients),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()

            ForwardMessageReplyTextField(
                modifier = Modifier.weight(1f),
                initialReplyTextProvider = forwardMessageUseCase,
                hintText = stringResource(R.string.post_forward_hint),
                onFileSelected = { uri ->
                    forwardMessageUseCase.onFileSelected(uri, context)
                },
                sendButton = { },
                textOptionsColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                onCreateForwardedMessage = { CompletableDeferred() },
                textOptionsTopContent = {
                    ForwardedMessagePreview(
                        modifier = Modifier.fillMaxWidth(),
                        forwardedPost = postToForward
                    )
                }
            )
        }
    }
}