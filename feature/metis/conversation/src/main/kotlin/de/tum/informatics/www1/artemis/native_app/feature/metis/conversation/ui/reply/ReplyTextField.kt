package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.OnTrueLaunchedEffect
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.FileValidationConstants
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.ReplyAutoCompletePopup
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.AutoCompleteUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.AutoCompleteUtil.manageAutoCompleteHints
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.ReplyAutoCompletePopupPositionProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownListContinuationUtil.continueListIfApplicable
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

internal const val TEST_TAG_CAN_CREATE_REPLY = "TEST_TAG_CAN_CREATE_REPLY"
internal const val TEST_TAG_REPLY_TEXT_FIELD = "TEST_TAG_REPLY_TEXT_FIELD"
internal const val TEST_TAG_REPLY_SEND_BUTTON = "TEST_TAG_REPLY_SEND_BUTTON"


@Composable
internal fun ReplyTextField(
    modifier: Modifier,
    replyMode: ReplyMode,
    onFileSelected: (Uri, (String) -> Unit) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    conversationName: String,
    surfaceShape: Shape = MaterialTheme.shapes.large,
    emojiService: EmojiService,
    placeholder: String? = null,
) {
    val replyState: ReplyState = rememberReplyState(replyMode, updateFailureState)
    var requestedAutoCompleteType by remember { mutableStateOf<AutoCompleteType?>(null) }
    // Keep the launcher outside AnimatedContent so changing editors cannot dispose an open picker.
    var pendingAttachmentTarget by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val filePickerLauncher = rememberFilePickerLauncher(LocalContext.current) { uri ->
        pendingAttachmentTarget?.let { target ->
            pendingAttachmentTarget = null
            onFileSelected(uri, target)
        }
    }
    val prepareAttachment = {
        val destination = replyMode
        pendingAttachmentTarget = (destination as? ReplyMode.NewMessage)?.onCaptureAttachmentTarget?.invoke()
            ?: { markup: String -> destination.onUpdate(MarkdownEditEngine.insertMarkup(destination.currentText.value, markup)) }
    }

    ProvideEmojis(emojiService) {
        Surface(
            modifier = modifier,
            color = Color.Transparent,
            shape = surfaceShape,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .navigationBarsPadding()
            ) {
                AutoCompletionDialog(
                    replyMode = replyMode,
                    requestedAutoCompleteType = requestedAutoCompleteType,
                    resetRequestedAutoCompleteType = { requestedAutoCompleteType = null }
                )

                AnimatedContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    targetState = replyState,
                    // Updating an editor session is not a send-state transition.
                    // One editor owns restoration, focus and the native IME.
                    contentKey = {
                        when (it) {
                            is ReplyState.CanCreate -> "create"
                            ReplyState.HasSentReply -> "sent"
                            is ReplyState.IsSendingReply -> "sending"
                        }
                    },
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
                                conversationName = conversationName,
                                placeholder = placeholder,
                                filePickerLauncher = filePickerLauncher,
                                onPrepareAttachment = prepareAttachment,
                                onRequestAutocompleteType = { requestedAutoCompleteType = it }
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
                                    .fillMaxWidth(),
                                onCancel = targetReplyState.onCancelSendReply,
                                title = stringResource(R.string.create_reply_sending_reply)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoCompletionDialog(
    replyMode: ReplyMode,
    requestedAutoCompleteType: AutoCompleteType?,
    resetRequestedAutoCompleteType: () -> Unit
) {
    val currentTextFieldValue by replyMode.currentText

    var boxWidth by remember { mutableIntStateOf(0) }
    var boxYOffset by remember { mutableIntStateOf(0) }

    var mayShowAutoCompletePopup by remember { mutableStateOf(true) }
    var requestDismissAutoCompletePopup by remember { mutableStateOf(false) }

    val tagChars = LocalReplyAutoCompleteHintProvider.current.legalTagChars
    val autoCompleteHints = manageAutoCompleteHints(currentTextFieldValue, requestedAutoCompleteType)

    LaunchedEffect(currentTextFieldValue) {
        mayShowAutoCompletePopup = true
        requestDismissAutoCompletePopup = false
    }

    OnTrueLaunchedEffect(requestDismissAutoCompletePopup) {
        delay(100)
        mayShowAutoCompletePopup = false
        resetRequestedAutoCompleteType()
    }

    val areHintsEmpty = autoCompleteHints.orEmpty().flatMap { it.items }.isEmpty()
    OnTrueLaunchedEffect(areHintsEmpty) {
        resetRequestedAutoCompleteType()
    }

    val showAutoCompletePopup = mayShowAutoCompletePopup && !areHintsEmpty
    if (!showAutoCompletePopup) {
        return
    }

    Box(
        modifier = Modifier
            .onSizeChanged {
                boxWidth = it.width
            }
            .onGloballyPositioned { coordinates ->
                val boxRootTopLeft = coordinates.localToRoot(Offset.Zero)
                boxYOffset = boxRootTopLeft.y.toInt()
            }
            .fillMaxWidth()
    ) {
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        ReplyAutoCompletePopup(
            autoCompleteCategories = autoCompleteHints.orEmpty(),
            targetWidth = with(LocalDensity.current) { boxWidth.toDp() },
            maxHeightFromScreen = with(LocalDensity.current) { boxYOffset.toDp() } - statusBarHeight,
            popupPositionProvider = ReplyAutoCompletePopupPositionProvider,
            performAutoComplete = { replacement ->
                val newTextFieldValue = AutoCompleteUtil.perform(
                    currentTextFieldValue,
                    tagChars,
                    replacement
                )

                replyMode.onUpdate(newTextFieldValue)
            },
            onDismissRequest = {
                requestDismissAutoCompletePopup = true
            }
        )
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
            Icon(imageVector = Icons.Default.Cancel, contentDescription = stringResource(R.string.reply_cancel))
        }
    }
}

@Composable
private fun CreateReplyUi(
    modifier: Modifier,
    replyMode: ReplyMode,
    conversationName: String,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onReply: () -> Unit,
    placeholder: String?,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onPrepareAttachment: () -> Unit,
    onRequestAutocompleteType: (AutoCompleteType) -> Unit
) {
    // Do not restore editor UI against a temporary provider identity while the
    // actual host/thread session is still loading.
    if (replyMode is ReplyMode.NewMessage && replyMode.editorSessionKey == null) return
    val currentTextFieldValue by replyMode.currentText
    val hint = if (replyMode is ReplyMode.EditMessage) {
        stringResource(R.string.reply_edit_placeholder)
    } else {
        placeholder ?: stringResource(R.string.reply_placeholder, conversationName)
    }

    Column(modifier) {
        if (replyMode is ReplyMode.EditMessage) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.reply_editing),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = replyMode.onCancelEditMessage) {
                    Text(stringResource(R.string.reply_cancel))
                }
            }
        }
        MarkdownTextField(
            modifier = Modifier.fillMaxWidth().testTag(TEST_TAG_REPLY_TEXT_FIELD),
            textFieldValue = currentTextFieldValue,
            editorSession = (replyMode as? ReplyMode.NewMessage)?.editorSessionKey ?: replyMode.currentText,
            hintText = AnnotatedString(hint),
            filePickerLauncher = filePickerLauncher,
            onPrepareAttachment = onPrepareAttachment,
            onTextChanged = { newValue ->
                replyMode.onUpdate(continueListIfApplicable(currentTextFieldValue.text, newValue))
            },
            focusRequester = focusRequester,
            showAutoCompletePopup = onRequestAutocompleteType,
            textFieldTrailingContent = {
                SendButton(
                    modifier = Modifier,
                    currentTextFieldValue = currentTextFieldValue,
                    replyMode = replyMode,
                    onReply = onReply,
                )
            },
        )
    }
}

@Composable
fun rememberFilePickerLauncher(
    context: Context,
    onFileSelected: (Uri) -> Unit
) = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult

    val mimeType = context.contentResolver.getType(uri)
    if (mimeType in FileValidationConstants.ALLOWED_MIME_TYPES) {
        onFileSelected(uri)
    } else {
        Toast.makeText(
            context,
            getString(context, R.string.markdown_textfield_unsupported_warning),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun SendButton(
    modifier: Modifier,
    currentTextFieldValue: TextFieldValue,
    replyMode: ReplyMode,
    onReply: () -> Unit
) {
    val canSend = MarkdownPresentation.parse(currentTextFieldValue.text).hasContent
    FilledIconButton(
        modifier = modifier.size(48.dp).testTag(TEST_TAG_REPLY_SEND_BUTTON),
        onClick = onReply,
        enabled = canSend,
        shape = CircleShape,
    ) {
        Icon(
            imageVector = if (replyMode is ReplyMode.EditMessage) Icons.Default.Done else Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(if (replyMode is ReplyMode.EditMessage) R.string.reply_save else R.string.reply_send),
            modifier = Modifier.size(24.dp),
        )
    }
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
            conversationName = "PreviewChat",
            onFileSelected = { _, _ -> },
            emojiService = EmojiServiceStub
        )
    }
}
