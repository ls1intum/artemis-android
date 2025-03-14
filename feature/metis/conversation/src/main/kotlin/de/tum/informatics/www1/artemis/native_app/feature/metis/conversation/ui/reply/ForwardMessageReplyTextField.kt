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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownListContinuationUtil.continueListIfApplicable
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil
import kotlinx.coroutines.Deferred

/**
 * An alternative version of the ReplyTextField that is used when forwarding a message.
 */
@Composable
fun ForwardMessageReplyTextField(
    modifier: Modifier,
    initialReplyTextProvider: InitialReplyTextProvider,
    hintText: String,
    onFileSelected: (Uri) -> Unit,
    isEmojiPickerEnabled: Boolean = false,
    textOptionsColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    backgroundColor: Color = BottomSheetDefaults.ContainerColor,
    sendButton: @Composable RowScope.() -> Unit = {},
    textOptionsTopContent: @Composable ColumnScope.() -> Unit,
    onCreateForwardedMessage: () -> Deferred<MetisModificationFailure?>,
) {
    val prevReplyContent by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()
    val context = LocalContext.current
    val newMessageText =
        initialReplyTextProvider.newMessageText.collectAsState(initial = TextFieldValue())

    val replyMode = ReplyMode.NewMessage(
        currentText = newMessageText,
        onUpdateTextUpstream = initialReplyTextProvider::updateInitialReplyText,
        onCreateNewMessageUpstream = onCreateForwardedMessage
    )

    val currentTextFieldValue by replyMode.currentText
    val filePickerLauncher = rememberFilePickerLauncher(context, onFileSelected)
    var requestedAutoCompleteType by remember { mutableStateOf<AutoCompleteType?>(null) }

    AutoCompletionDialog(
        replyMode = replyMode,
        requestedAutoCompleteType = requestedAutoCompleteType,
        resetRequestedAutoCompleteType = { requestedAutoCompleteType = null }
    )

    MarkdownTextField(
        modifier = modifier.fillMaxSize(),
        textFieldValue = currentTextFieldValue,
        hintText = AnnotatedString(hintText),
        filePickerLauncher = filePickerLauncher,
        backgroundColor = backgroundColor,
        textOptionsColor = textOptionsColor,
        onTextChanged = { newValue ->
            val finalValue = continueListIfApplicable(prevReplyContent, newValue)
            replyMode.onUpdate(finalValue)
        },
        isTextOptionsInitiallyVisible = false,
        focusRequester = focusRequester,
        isEmojiPickerEnabled = isEmojiPickerEnabled,
        alignOptionsAtBottom = true,
        showAutoCompletePopup = {
            requestedAutoCompleteType = it
            val newTextFieldValue = MarkdownStyleUtil.apply(
                style = when (it) {
                    AutoCompleteType.USERS -> MarkdownStyle.UserMention
                    AutoCompleteType.CHANNELS -> MarkdownStyle.ChannelMention
                    AutoCompleteType.LECTURES -> MarkdownStyle.LectureMention
                    AutoCompleteType.EXERCISES -> MarkdownStyle.ExerciseMention
                    AutoCompleteType.FAQS -> MarkdownStyle.FaqMention
                },
                currentTextFieldValue = currentTextFieldValue,
            )
            replyMode.onUpdate(newTextFieldValue)
        },
        textFieldTrailingContent = sendButton,
        textOptionsTopContent = textOptionsTopContent,
        formattingOptionButtons = {
            FormattingOptions(
                textOptionsColor,
                applyMarkdownStyle = {
                    val newTextFieldValue = MarkdownStyleUtil.apply(
                        style = it,
                        currentTextFieldValue = currentTextFieldValue,
                    )
                    replyMode.onUpdate(newTextFieldValue)
                }
            )
        }
    )
}