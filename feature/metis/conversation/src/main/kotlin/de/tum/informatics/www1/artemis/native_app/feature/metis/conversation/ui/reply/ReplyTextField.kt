package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ReplyState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TEST_TAG_CAN_CREATE_REPLY),
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

    val currentTextFieldValue by replyMode.currentText

    LaunchedEffect(displayTextField, currentTextFieldValue) {
        if (!displayTextField && currentTextFieldValue.text.isNotBlank() && prevReplyContent.isBlank()) {
            focusRequester.requestFocus()
            displayTextField = true
        }

        prevReplyContent = currentTextFieldValue.text
    }

    val replyAutoCompleteHintProvider = LocalReplyAutoCompleteHintProvider.current
    var replyAutoCompleteHintProducer: Flow<DataState<List<AutoCompleteCategory>>>? by remember {
        mutableStateOf(
            null
        )
    }

    val tagChars = replyAutoCompleteHintProvider.legalTagChars
    LaunchedEffect(currentTextFieldValue, replyAutoCompleteHintProvider) {
        // Check that no text is selected, and instead we have a simple cursor
        replyAutoCompleteHintProducer = if (currentTextFieldValue.selection.collapsed) {
            // Gather the last word, meaning the characters from the last whitespace until the cursor.
            val lastWord = currentTextFieldValue
                .getTextBeforeSelection(Int.MAX_VALUE)
                .takeLastWhileTag(tagChars)

            if (tagChars.any { lastWord.startsWith(it) }) {
                replyAutoCompleteHintProvider.produceAutoCompleteHints(
                    lastWord.first(),
                    lastWord.substring(1)
                )
            } else null
        } else null
    }

    Box(modifier = modifier) {
        if (displayTextField || currentTextFieldValue.text.isNotBlank()) {
            val replyAutoCompleteHints = replyAutoCompleteHintProducer
                ?.collectAsState(DataState.Loading())
                ?.value
                ?.orElse(emptyList())
                .orEmpty()

            var latestValidAutoCompleteHints: List<AutoCompleteCategory>? by remember {
                mutableStateOf(null)
            }

            LaunchedEffect(replyAutoCompleteHintProducer, replyAutoCompleteHints) {
                if (replyAutoCompleteHintProducer == null) {
                    latestValidAutoCompleteHints = null
                } else if (replyAutoCompleteHints.isNotEmpty()) {
                    latestValidAutoCompleteHints = replyAutoCompleteHints
                }
            }

            if (latestValidAutoCompleteHints.orEmpty().isNotEmpty()) {
                ReplyAutoCompletePopup(
                    autoCompleteCategories = latestValidAutoCompleteHints.orEmpty(),
                    performAutoComplete = { replacement ->
                        val replacementStart = currentTextFieldValue
                            .getTextBeforeSelection(Int.MAX_VALUE)
                            .indexOfLastWhileTag(tagChars)

                        val replacementEnd = currentTextFieldValue.selection.min

                        val newText = currentTextFieldValue
                            .text
                            .replaceRange(replacementStart, replacementEnd, replacement)

                        replyMode.currentText.value = currentTextFieldValue.copy(
                            text = newText,
                            // Put cursor after replacement.
                            selection = TextRange(replacementStart + replacement.length)
                        )
                    }
                )
            }

            MarkdownTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .testTag(TEST_TAG_REPLY_TEXT_FIELD),
                textFieldValue = currentTextFieldValue,
                onTextChanged = replyMode::onUpdate,
                focusRequester = focusRequester,
                onFocusLost = {
                    if (displayTextField && currentTextFieldValue.text.isEmpty()) {
                        displayTextField = false
                    }
                },
                sendButton = {
                    IconButton(
                        modifier = Modifier.testTag(TEST_TAG_REPLY_SEND_BUTTON),
                        onClick = onReply,
                        enabled = currentTextFieldValue.text.isNotBlank()
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
            replyMode.onUpdate(TextFieldValue(text = ""))
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

@Composable
@Preview
private fun ReplyTextFieldPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ReplyTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            replyMode = ReplyMode.NewMessage { CompletableDeferred() },
            updateFailureState = {}
        )
    }
}

/**
 * Custom implementation that takes until either the second whitespace has been found or a tag character has been found
 */
private fun CharSequence.takeLastWhileTag(tagChars: List<Char>): CharSequence {
    return subSequence(indexOfLastWhileTag(tagChars), length)
}

private fun CharSequence.indexOfLastWhileTag(tagChars: List<Char>): Int {
    var foundWhitespace = false

    for (index in lastIndex downTo 0) {
        val currentChar = this[index]

        if (currentChar.isWhitespace() && foundWhitespace) {
            return index + 1
        } else if (currentChar in tagChars) {
            return index
        } else if (currentChar.isWhitespace()) {
            foundWhitespace = true
        }
    }
    return 0
}