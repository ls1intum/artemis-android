package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply

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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common.MarkdownTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.ReplyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun ReplyUi(
    modifier: Modifier,
    replyContent: String,
    updateReplyContent: (String) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    createReply: (String) -> Deferred<MetisModificationFailure?>
) {
    val scope = rememberCoroutineScope()

    val replyState: ReplyState by remember {
        val state = mutableStateOf<ReplyState>(ReplyState.HasSentReply)

        state.value = createCanCreateReplyState(
            coroutineScope = scope,
            updateReplyState = { state.value = it },
            updateFailureState = updateFailureState,
            clearReplyContent = { updateReplyContent("") },
            createReply = createReply
        )

        state
    }

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
                            modifier = Modifier.fillMaxWidth(),
                            onReply = targetReplyState.onCreateReply,
                            replyContent = replyContent,
                            updateReplyContent = updateReplyContent
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
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

                            IconButton(onClick = targetReplyState.onCancelSendReply) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateReplyUi(
    modifier: Modifier,
    replyContent: String,
    updateReplyContent: (String) -> Unit,
    onReply: (String) -> Unit
) {
    var displayTextField: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (displayTextField) {
            val focusRequest = remember { FocusRequester() }

            MarkdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                text = replyContent,
                onTextChanged = updateReplyContent,
                focusRequester = focusRequest,
                onFocusLost = {
                    if (displayTextField && replyContent.isEmpty()) {
                        displayTextField = false
                    }
                },
                sendButton = {
                    IconButton(
                        onClick = { onReply(replyContent) },
                        enabled = replyContent.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    }
                }
            )

            LaunchedEffect(key1 = displayTextField) {
                focusRequest.requestFocus()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { displayTextField = true }
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
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )
            }
        }

    }
}

/**
 * Factory to not duplicate can create reply state code.
 * @param updateReplyState set the UI reply state state
 * @param updateFailureState set failure state to show dialog if creating reply failed.
 * @param clearReplyContent set the reply content back to an empty string
 */
private fun createCanCreateReplyState(
    coroutineScope: CoroutineScope,
    updateReplyState: (ReplyState) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    clearReplyContent: () -> Unit,
    createReply: (String) -> Deferred<MetisModificationFailure?>
): ReplyState.CanCreate {
    val reset = {
        updateReplyState(
            createCanCreateReplyState(
                coroutineScope,
                updateReplyState,
                updateFailureState,
                clearReplyContent,
                createReply
            )
        )
    }

    return ReplyState.CanCreate(
        onCreateReply = { content ->
            val job = createReply(content)

            coroutineScope.launch {
                val failure = job.await()

                if (failure == null) {
                    updateReplyState(ReplyState.HasSentReply)
                    clearReplyContent()

                    // Show success for 1 seconds. Then allow input of new reply.
                    delay(1.seconds)
                    reset()
                } else {
                    updateFailureState(failure)
                    reset()
                }
            }

            updateReplyState(
                ReplyState.IsSendingReply(
                    onCancelSendReply = {
                        job.cancel()

                        updateReplyState(
                            createCanCreateReplyState(
                                coroutineScope,
                                updateReplyState,
                                updateFailureState,
                                clearReplyContent,
                                createReply
                            )
                        )
                    }
                )
            )

        }
    )
}

@Composable
@Preview
private fun ReplyUiPreview() {
    var replyContent by remember { mutableStateOf("") }

    CreateReplyUi(
        modifier = Modifier.fillMaxWidth(),
        onReply = {},
        replyContent = replyContent,
        updateReplyContent = { replyContent = it }
    )
}