package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.MetisModificationTask
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownListContinuationUtil.continueListIfApplicable

/**
 * An alternative version of the ReplyTextField that is used when forwarding a message.
 */
@Composable
fun ForwardMessageReplyTextField(
    modifier: Modifier,
    initialReplyTextProvider: InitialReplyTextProvider,
    hintText: String,
    onFileSelected: (Uri, (String) -> Unit) -> Unit,
    isEmojiPickerEnabled: Boolean = false,
    backgroundColor: Color = BottomSheetDefaults.ContainerColor,
    sendButton: @Composable RowScope.() -> Unit = {},
    textOptionsTopContent: @Composable ColumnScope.() -> Unit,
    onCreateForwardedMessage: () -> MetisModificationTask,
) {
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val newMessageText =
        initialReplyTextProvider.newMessageText.collectAsState(initial = TextFieldValue())
    val sessionKeys = remember(initialReplyTextProvider) { initialReplyTextProvider.editorSessionKey }
    val editorSessionKey by sessionKeys.collectAsState(initial = null)

    val replyMode = ReplyMode.NewMessage(
        currentText = newMessageText,
        onUpdateTextUpstream = initialReplyTextProvider::updateInitialReplyText,
        onCreateNewMessageUpstream = onCreateForwardedMessage
    )

    val currentTextFieldValue by replyMode.currentText
    var pendingAttachmentTarget by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val captureTarget = {
        initialReplyTextProvider.captureAttachmentTarget()
            ?: { markup: String ->
                replyMode.onUpdate(MarkdownEditEngine.insertMarkup(replyMode.currentText.value, markup))
            }
    }
    val filePickerLauncher = rememberFilePickerLauncher(context) { uri ->
        val captured = pendingAttachmentTarget ?: captureTarget()
        pendingAttachmentTarget = null
        onFileSelected(uri, captured)
    }
    var requestedAutoCompleteType by remember { mutableStateOf<AutoCompleteType?>(null) }

    if (editorSessionKey == null) return

    AutoCompletionDialog(
        replyMode = replyMode,
        requestedAutoCompleteType = requestedAutoCompleteType,
        resetRequestedAutoCompleteType = { requestedAutoCompleteType = null }
    )

    MarkdownTextField(
        modifier = modifier.fillMaxSize(),
        textFieldValue = currentTextFieldValue,
        editorSession = editorSessionKey,
        hintText = AnnotatedString(hintText),
        filePickerLauncher = filePickerLauncher,
        onPrepareAttachment = { pendingAttachmentTarget = captureTarget() },
        backgroundColor = backgroundColor,
        onTextChanged = { newValue ->
            val finalValue = continueListIfApplicable(currentTextFieldValue.text, newValue)
            replyMode.onUpdate(finalValue)
        },
        focusRequester = focusRequester,
        isEmojiPickerEnabled = isEmojiPickerEnabled,
        alignOptionsAtBottom = true,
        showAutoCompletePopup = {
            requestedAutoCompleteType = it
        },
        textFieldTrailingContent = sendButton,
        textOptionsTopContent = textOptionsTopContent,

    )
}
