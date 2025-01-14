package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownListContinuationUtil.continueListIfApplicable
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ReplyState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

internal const val TEST_TAG_CAN_CREATE_REPLY = "TEST_TAG_CAN_CREATE_REPLY"
internal const val TEST_TAG_REPLY_TEXT_FIELD = "TEST_TAG_REPLY_TEXT_FIELD"
internal const val TEST_TAG_REPLY_SEND_BUTTON = "TEST_TAG_REPLY_SEND_BUTTON"
internal const val TEST_TAG_UNFOCUSED_TEXT_FIELD = "TEST_TAG_UNFOCUSED_TEXT_FIED"

private const val DisabledContentAlpha = 0.75f

@Composable
internal fun ReplyTextField(
    modifier: Modifier,
    replyMode: ReplyMode,
    onFileSelected: (Uri) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    title: String
) {
    val replyState: ReplyState = rememberReplyState(replyMode, updateFailureState)

    Surface(
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large
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
                            onReply = { targetReplyState.onCreateReply() },
                            title = stringResource(R.string.create_reply_click_to_write, title),
                            onFileSelected = { uri -> onFileSelected(uri) }
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
                            onCancel = targetReplyState.onCancelSendReply,
                            title = stringResource(R.string.create_reply_sending_reply)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SendingReplyUi(modifier: Modifier, onCancel: () -> Unit, title: String?) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(30.dp)
        )

        Text(
            text = title.toString(),
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
    onReply: () -> Unit,
    onFileSelected: (Uri) -> Unit,
    title: String?
) {
    var prevReplyContent by remember { mutableStateOf("") }
    var displayTextField: Boolean by remember { mutableStateOf(false) }
    var requestFocus: Boolean by remember { mutableStateOf(false) }

    val currentTextFieldValue by replyMode.currentText

    var mayShowAutoCompletePopup by remember { mutableStateOf(true) }
    var requestDismissAutoCompletePopup by remember { mutableStateOf(false) }

    LaunchedEffect(displayTextField, currentTextFieldValue) {
        if (!displayTextField && currentTextFieldValue.text.isNotBlank() && prevReplyContent.isBlank()) {
            displayTextField = true
        }

        prevReplyContent = currentTextFieldValue.text
        mayShowAutoCompletePopup = true
        requestDismissAutoCompletePopup = false
    }

    LaunchedEffect(requestDismissAutoCompletePopup) {
        if (requestDismissAutoCompletePopup) {
            delay(100)
            mayShowAutoCompletePopup = false
        }
    }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (displayTextField || currentTextFieldValue.text.isNotBlank()) {
                val tagChars = LocalReplyAutoCompleteHintProvider.current.legalTagChars
                val autoCompleteHints = manageAutoCompleteHints(currentTextFieldValue)

                var textFieldWidth by remember { mutableIntStateOf(0) }
                var popupMaxHeight by remember { mutableIntStateOf(0) }

                val showAutoCompletePopup = mayShowAutoCompletePopup
                        && autoCompleteHints.orEmpty().flatMap { it.items }.isNotEmpty()

                if (showAutoCompletePopup) {
                    ReplyAutoCompletePopup(
                        autoCompleteCategories = autoCompleteHints.orEmpty(),
                        targetWidth = with(LocalDensity.current) { textFieldWidth.toDp() },
                        maxHeightFromScreen = with(LocalDensity.current) { popupMaxHeight.toDp() },
                        popupPositionProvider = ReplyAutoCompletePopupPositionProvider,
                        performAutoComplete = { replacement ->
                            replyMode.onUpdate(
                                performAutoComplete(
                                    currentTextFieldValue,
                                    tagChars,
                                    replacement
                                )
                            )
                        },
                        onDismissRequest = {
                            requestDismissAutoCompletePopup = true
                        }
                    )
                }

                MarkdownTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { textFieldWidth = it.width }
                        .onGloballyPositioned { coordinates ->
                            val textFieldRootTopLeft = coordinates.localToRoot(Offset.Zero)
                            popupMaxHeight = textFieldRootTopLeft.y.toInt()
                        }
                        .padding(8.dp)
                        .testTag(TEST_TAG_REPLY_TEXT_FIELD),
                    textFieldValue = currentTextFieldValue,
                    onTextChanged = { newValue ->
                        val finalValue = continueListIfApplicable(prevReplyContent, newValue)
                        replyMode.onUpdate(finalValue)
                    },
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
                                    is ReplyMode.NewMessage -> Icons.AutoMirrored.Filled.Send
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
                    },
                    onFileSelected = { uri ->
                        onFileSelected(uri)
                    },
                    formattingOptionButtons = {
                        FormattingOptions(
                            currentTextFieldValue = currentTextFieldValue,
                            onTextChanged = replyMode::onUpdate
                        )
                    },
                )

                LaunchedEffect(requestFocus) {
                    if (requestFocus) {
                        focusRequester.requestFocus()
                        requestFocus = false
                    }
                }
            } else {
                UnfocusedPreviewReplyTextField({
                    displayTextField = true
                    requestFocus = true
                }, title = title)
            }
        }

    }
}

enum class MarkdownStyle(val startTag: String, val endTag: String) {
    Bold("**", "**"),
    Italic("*", "*"),
    Underline("<ins>", "</ins>"),
    InlineCode("`", "`"),
    CodeBlock("```", "```"),
    Blockquote("> ", ""),
    OrderedList("1. ", ""),
    UnorderedList("- ", "")
}

@Composable
private fun FormattingOptions(
    currentTextFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit
) {

    var isDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Bold Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.Bold,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "B",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Italic Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.Italic,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "I",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            )
        }

        // Underline Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.Underline,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "U",
                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
            )
        }

        // Inline Code Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.InlineCode,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "</>",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )
        }

        // Code Block Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.CodeBlock,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "{ }",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )
        }

        // Blockquote Button
        IconButton(onClick = {
            applyMarkdownStyle(
                style = MarkdownStyle.Blockquote,
                currentTextFieldValue = currentTextFieldValue,
                onTextChanged = onTextChanged
            )
        }) {
            Text(
                text = "\"",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            )
        }

        Box(modifier = Modifier.align(Alignment.Top)) {
            IconButton(
                onClick = { isDropdownExpanded = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                // Unordered List item
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.reply_format_unordered)) },
                    onClick = {
                        isDropdownExpanded = false
                        applyMarkdownStyle(
                            style = MarkdownStyle.UnorderedList,
                            currentTextFieldValue = currentTextFieldValue,
                            onTextChanged = onTextChanged
                        )
                    }
                )
                // Ordered List item
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.reply_format_ordered)) },
                    onClick = {
                        isDropdownExpanded = false
                        applyMarkdownStyle(
                            style = MarkdownStyle.OrderedList,
                            currentTextFieldValue = currentTextFieldValue,
                            onTextChanged = onTextChanged
                        )
                    }
                )
            }
        }
    }
}

private fun applyMarkdownStyle(
    style: MarkdownStyle,
    currentTextFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit
) {
    val selection = currentTextFieldValue.selection
    val text = currentTextFieldValue.text

    val startTag = style.startTag
    val endTag = style.endTag

    if (selection.collapsed) {
        // No text selected
        if (style == MarkdownStyle.CodeBlock) {
            // Insert code block with newlines
            val newText = text.substring(0, selection.start) +
                    "$startTag\n\n$endTag" +
                    text.substring(selection.end)
            val newCursorPosition = selection.start + startTag.length + 1
            onTextChanged(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursorPosition, newCursorPosition)
                )
            )
        } else {
            // Other styles
            val newText = text.substring(
                0,
                selection.start
            ) + startTag + endTag + text.substring(selection.end)
            val newCursorPosition = selection.start + startTag.length
            onTextChanged(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursorPosition, newCursorPosition)
                )
            )
        }
    } else {
        val selectedText = text.substring(selection.start, selection.end)
        if (style == MarkdownStyle.CodeBlock) {
            val newText = text.substring(0, selection.start) +
                    "$startTag\n$selectedText\n$endTag" +
                    text.substring(selection.end)
            val newSelection = TextRange(
                selection.start + startTag.length + 1,
                selection.end + startTag.length + 1
            )
            onTextChanged(
                TextFieldValue(
                    text = newText,
                    selection = newSelection
                )
            )
        } else {
            val newText = text.substring(0, selection.start) +
                    startTag +
                    selectedText +
                    endTag +
                    text.substring(selection.end)
            val newSelection = TextRange(selection.end + startTag.length + endTag.length)
            onTextChanged(
                TextFieldValue(
                    text = newText,
                    selection = newSelection
                )
            )
        }
    }
}


@Composable
private fun UnfocusedPreviewReplyTextField(onRequestShowTextField: () -> Unit, title: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRequestShowTextField)
            .padding(horizontal = 16.dp)
            .testTag(TEST_TAG_UNFOCUSED_TEXT_FIELD),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.toString(),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = DisabledContentAlpha)
        )
    }
}

private fun performAutoComplete(
    textFieldValue: TextFieldValue,
    tagChars: List<Char>,
    replacement: String
): TextFieldValue {
    // Perform replace in text
    val replacementStart = textFieldValue
        .getTextBeforeSelection(Int.MAX_VALUE)
        .indexOfLastWhileTag(tagChars)
        ?: return textFieldValue

    val replacementEnd = textFieldValue.selection.min

    val newText = textFieldValue
        .text
        .replaceRange(replacementStart, replacementEnd, replacement)

    return textFieldValue.copy(
        text = newText,
        // Put cursor after replacement.
        selection = TextRange(replacementStart + replacement.length)
    )
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
    var displaySendSuccess by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(job = isCreatingReplyJob) { failure ->
        if (failure == null) {
            replyMode.onUpdate(TextFieldValue(text = ""))

            // Only show for edit.
            displaySendSuccess = replyMode is ReplyMode.EditMessage
        } else {
            updateFailureState(failure)
        }

        isCreatingReplyJob = null
    }

    LaunchedEffect(key1 = displaySendSuccess) {
        if (displaySendSuccess) {
            delay(1.seconds)

            displaySendSuccess = false
        }
    }

    return remember(isCreatingReplyJob, displaySendSuccess, replyMode) {
        when {
            isCreatingReplyJob != null -> ReplyState.IsSendingReply {
                isCreatingReplyJob?.cancel()
                isCreatingReplyJob = null
            }

            displaySendSuccess -> ReplyState.HasSentReply
            else -> ReplyState.CanCreate {
                isCreatingReplyJob = when (replyMode) {
                    is ReplyMode.EditMessage -> replyMode.onEditMessage()
                    is ReplyMode.NewMessage -> replyMode.onCreateNewMessage()
                }
            }
        }
    }
}

/**
 * @return a list of auto complete hints that should be displayed, or null if no auto complete hints are to be displayed.
 */
@Composable
private fun manageAutoCompleteHints(textFieldValue: TextFieldValue): List<AutoCompleteCategory>? {
    val replyAutoCompleteHintProvider = LocalReplyAutoCompleteHintProvider.current
    var replyAutoCompleteHintProducer: Flow<DataState<List<AutoCompleteCategory>>>? by remember {
        mutableStateOf(
            null
        )
    }

    val tagChars = replyAutoCompleteHintProvider.legalTagChars
    LaunchedEffect(textFieldValue, replyAutoCompleteHintProvider) {
        // Check that no text is selected, and instead we have a simple cursor
        replyAutoCompleteHintProducer =
            textFieldValue.getAutoCompleteReplacementTextFirstIndex(tagChars)?.let { tagIndex ->
                val tagChar = textFieldValue.text[tagIndex]
                val replacementWord = if (textFieldValue.text.length > tagIndex + 1) {
                    textFieldValue.text.substring(tagIndex + 1).takeWhileTag()
                } else ""

                replyAutoCompleteHintProvider.produceAutoCompleteHints(tagChar, replacementWord)
            }
    }

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

    return latestValidAutoCompleteHints
}

/**
 * @return the index of the tagging char of the current replacement text, or null if the user is currently
 * not entering a replaceable text.
 */
private fun TextFieldValue.getAutoCompleteReplacementTextFirstIndex(tagChars: List<Char>): Int? {
    return if (selection.collapsed) {
        // Gather the last word, meaning the characters from the last whitespace until the cursor.
        val tagCharIndex = getTextBeforeSelection(Int.MAX_VALUE)
            .indexOfLastWhileTag(tagChars)
            ?: return null

        val tagChar = text[tagCharIndex]

        if (tagChar in tagChars) tagCharIndex else null
    } else null
}

@Composable
@Preview
private fun ReplyTextFieldPreview() {
    val text = remember { mutableStateOf(TextFieldValue()) }

    Box(modifier = Modifier.fillMaxSize()) {
        ReplyTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            replyMode = ReplyMode.NewMessage(
                text,
                onUpdateTextUpstream = { text.value = it }
            ) {
                CompletableDeferred()
            },
            updateFailureState = {},
            title = "Replying..",
            onFileSelected = { _ -> }
        )
    }
}

private fun CharSequence.indexOfLastWhileTag(tagChars: List<Char>): Int? {
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

    return if (isEmpty()) null else 0
}

/**
 * Takes characters until the end of the string or a second whitespace has been found
 */
private fun String.takeWhileTag(): String {
    var foundWhitespace = false

    for (index in indices) {
        val currentChar = this[index]

        if (currentChar.isWhitespace() && foundWhitespace) {
            return substring(0, index)
        } else if (currentChar.isWhitespace()) {
            foundWhitespace = true
        }
    }

    return this
}

