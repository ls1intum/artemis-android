package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ReplyState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal const val TEST_TAG_CAN_CREATE_REPLY = "TEST_TAG_CAN_CREATE_REPLY"
internal const val TEST_TAG_REPLY_TEXT_FIELD = "TEST_TAG_REPLY_TEXT_FIELD"
internal const val TEST_TAG_REPLY_SEND_BUTTON = "TEST_TAG_REPLY_SEND_BUTTON"

private const val DisabledContentAlpha = 0.75f

@Composable
internal fun ReplyTextField(
    modifier: Modifier,
    replyMode: ReplyMode,
    updateFailureState: (MetisModificationFailure?) -> Unit
) {
    val replyState: ReplyState = rememberReplyState(replyMode, updateFailureState)

    Surface(
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(5)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                targetState = replyState,
                label = "CanCreate -> HasSentReply -> IsSendingReply"
            ) { targetReplyState ->
                when (targetReplyState) {
                    is ReplyState.CanCreate -> {
                        CreateReplyUi(
                            modifier = Modifier.fillMaxWidth().testTag(TEST_TAG_CAN_CREATE_REPLY),
                            replyMode = replyMode,
                            onReply = { targetReplyState.onCreateReply() }
                        )
                    }

                    ReplyState.HasSentReply -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    is ReplyState.IsSendingReply -> {
                        SendingReplyUi(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            onCancel = targetReplyState.onCancelSendReply
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SendingReplyUi(modifier: Modifier, onCancel: () -> Unit) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(30.dp)
        )

        Text(
            text = stringResource(id = R.string.create_answer_sending_reply),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onCancel) {
            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
        }
    }
}

@Composable
private fun CreateReplyUi(
    modifier: Modifier,
    replyMode: ReplyMode,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onReply: () -> Unit
) {
    var prevReplyContent by remember { mutableStateOf("") }
    var displayTextField: Boolean by remember { mutableStateOf(false) }

    val currentText by replyMode.currentText

    LaunchedEffect(displayTextField, currentText) {
        if (!displayTextField && currentText.isNotBlank() && prevReplyContent.isBlank()) {
            focusRequester.requestFocus()
            displayTextField = true
        }

        prevReplyContent = currentText
    }

    Box(modifier = modifier) {
        if (displayTextField || currentText.isNotBlank()) {
            MarkdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .testTag(TEST_TAG_REPLY_TEXT_FIELD),
                text = currentText,
                onTextChanged = replyMode::onUpdateText,
                focusRequester = focusRequester,
                onFocusLost = {
                    if (displayTextField && currentText.isEmpty()) {
                        displayTextField = false
                    }
                },
                sendButton = {
                    IconButton(
                        modifier = Modifier.testTag(TEST_TAG_REPLY_SEND_BUTTON),
                        onClick = onReply,
                        enabled = currentText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = when (replyMode) {
                                is ReplyMode.EditMessage -> Icons.Default.Edit
                                is ReplyMode.NewMessage -> Icons.Default.Send
                            },
                            contentDescription = null
                        )
                    }
                },
                topRightButton = {
                    if (replyMode is ReplyMode.EditMessage) {
                        IconButton(onClick = replyMode.onCancelEditMessage) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = displayTextField) {
                focusRequester.requestFocus()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        displayTextField = true
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.create_answer_click_to_write),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = DisabledContentAlpha)
                )
            }
        }
    }
}

/**
 * Cycles through the reply state. When create reply is clicked, switches to sending reply.
 * If sending the reply was successful, shows has sent reply shortly.
 */
@Composable
private fun rememberReplyState(
    replyMode: ReplyMode,
    updateFailureState: (MetisModificationFailure?) -> Unit
): ReplyState {
    var isCreatingReplyJob: Deferred<MetisModificationFailure?>? by remember { mutableStateOf(null) }
    var hasSentReply by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(job = isCreatingReplyJob) { failure ->
        if (failure == null) {
            replyMode.onUpdateText("")
            hasSentReply = true
        } else {
            updateFailureState(failure)
        }

        isCreatingReplyJob = null
    }

    LaunchedEffect(key1 = hasSentReply) {
        if (hasSentReply) {
            delay(1.seconds)

            hasSentReply = false
        }
    }

    return remember(isCreatingReplyJob, hasSentReply, replyMode) {
        when {
            isCreatingReplyJob != null -> ReplyState.IsSendingReply {
                isCreatingReplyJob?.cancel()
                isCreatingReplyJob = null
            }

            hasSentReply -> ReplyState.HasSentReply
            else -> ReplyState.CanCreate {
                isCreatingReplyJob = when (replyMode) {
                    is ReplyMode.EditMessage -> replyMode.onEditMessage()
                    is ReplyMode.NewMessage -> replyMode.onCreateNewMessage()
                }
            }
        }
    }
}
