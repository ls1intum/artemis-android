package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.FileValidationConstants
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.emoji.ProvideEmojis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.ReplyAutoCompletePopup
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.AutoCompleteUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.AutoCompleteUtil.manageAutoCompleteHints
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.ReplyAutoCompletePopupPositionProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownListContinuationUtil.continueListIfApplicable
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

internal const val TEST_TAG_CAN_CREATE_REPLY = "TEST_TAG_CAN_CREATE_REPLY"
internal const val TEST_TAG_REPLY_TEXT_FIELD = "TEST_TAG_REPLY_TEXT_FIELD"
internal const val TEST_TAG_REPLY_SEND_BUTTON = "TEST_TAG_REPLY_SEND_BUTTON"
internal const val TEST_TAG_UNFOCUSED_TEXT_FIELD = "TEST_TAG_UNFOCUSED_TEXT_FIELD"


@Composable
internal fun ReplyTextField(
    modifier: Modifier,
    replyMode: ReplyMode,
    onFileSelected: (Uri) -> Unit,
    updateFailureState: (MetisModificationFailure?) -> Unit,
    conversationName: String
) {
    val replyState: ReplyState = rememberReplyState(replyMode, updateFailureState)
    val requestedAutoCompleteType = remember { mutableStateOf<AutoCompleteType?>(null) }

    ProvideEmojis {
        Surface(
            modifier = modifier,
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 100f
                )
            ),
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .navigationBarsPadding()
            ) {
                AutoCompletionDialog(
                    replyMode = replyMode,
                    requestedAutoCompleteType = requestedAutoCompleteType,
                )

                AnimatedContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing, vertical = 8.dp)
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
                                conversationName = conversationName,
                                onFileSelected = { uri -> onFileSelected(uri) },
                                onRequestAutocompleteType = { requestedAutoCompleteType.value = it }
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
private fun AutoCompletionDialog(
    replyMode: ReplyMode,
    requestedAutoCompleteType: MutableState<AutoCompleteType?>,
) {
    val currentTextFieldValue by replyMode.currentText

    var boxWidth by remember { mutableIntStateOf(0) }
    var boxYOffset by remember { mutableIntStateOf(0) }

    var mayShowAutoCompletePopup by remember { mutableStateOf(true) }
    var requestDismissAutoCompletePopup by remember { mutableStateOf(false) }

    val tagChars = LocalReplyAutoCompleteHintProvider.current.legalTagChars
    val autoCompleteHints = manageAutoCompleteHints(currentTextFieldValue, requestedAutoCompleteType.value)

    LaunchedEffect( currentTextFieldValue) {
        mayShowAutoCompletePopup = true
        requestDismissAutoCompletePopup = false
    }

    LaunchedEffect(requestDismissAutoCompletePopup) {
        if (requestDismissAutoCompletePopup) {
            delay(100)
            mayShowAutoCompletePopup = false
            requestedAutoCompleteType.value = null
        }
    }

    val showAutoCompletePopup = mayShowAutoCompletePopup
            && autoCompleteHints.orEmpty().flatMap { it.items }.isNotEmpty()

    if (!showAutoCompletePopup) {
        return
    }

    Box(
        modifier = Modifier
            .onSizeChanged {
                boxWidth = it.width
                println("boxWidth: $boxWidth")
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
            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
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
    onFileSelected: (Uri) -> Unit,
    onRequestAutocompleteType: (AutoCompleteType) -> Unit
) {
    var prevReplyContent by remember { mutableStateOf("") }
    var displayTextField: Boolean by remember { mutableStateOf(false) }
    var requestFocus: Boolean by remember { mutableStateOf(false) }

    val currentTextFieldValue by replyMode.currentText

    val hintText = buildAnnotatedString {
        append(stringResource(R.string.create_reply_click_to_write_prefix) + " '")
        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
            append(conversationName)
        }
        append("'")
    }

    val context = LocalContext.current
    val filePickerLauncher = rememberFilePickerLauncher(context, onFileSelected)

    LaunchedEffect(displayTextField, currentTextFieldValue) {
        if (!displayTextField && currentTextFieldValue.text.isNotBlank() && prevReplyContent.isBlank()) {
            displayTextField = true
        }

        prevReplyContent = currentTextFieldValue.text
    }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            requestFocus = false
        }
    }

    Box(modifier.fillMaxWidth()) {
        val showUnfocusedReplyField = !displayTextField && currentTextFieldValue.text.isBlank()
        if (showUnfocusedReplyField) {
            UnfocusedPreviewReplyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TEST_TAG_UNFOCUSED_TEXT_FIELD),
                hintText = hintText,
                filePickerLauncher = filePickerLauncher,
                onRequestShowTextField = {
                    displayTextField = true
                    requestFocus = true
                }
            )
            return
        }

        MarkdownTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TEST_TAG_REPLY_TEXT_FIELD),
            textFieldValue = currentTextFieldValue,
            hintText = hintText,
            filePickerLauncher = filePickerLauncher,
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
            showAutoCompletePopup = {
                onRequestAutocompleteType(it)
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
            textFieldTrailingContent = {
                SendButton(
                    modifier = Modifier,
                    currentTextFieldValue = currentTextFieldValue,
                    replyMode = replyMode,
                    onReply = onReply
                )

                if (replyMode is ReplyMode.EditMessage) {
                    CancelButton(
                                onClick = replyMode.onCancelEditMessage
                    )
                }
            },
            formattingOptionButtons = {
                FormattingOptions(
                    applyMarkdownStyle = {
                        val newTextFieldValue = MarkdownStyleUtil.apply(
                            style = it,
                            currentTextFieldValue = currentTextFieldValue,
                        )
                        replyMode.onUpdate(newTextFieldValue)
                    }
                )
            },
        )
    }
}

@Composable
private fun rememberFilePickerLauncher(
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
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .then(
                if (currentTextFieldValue.text.isBlank()) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f
                        )
                    )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                }
            )
    ) {
        IconButton(
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.Center)
                .testTag(TEST_TAG_REPLY_SEND_BUTTON),
            onClick = onReply,
            enabled = currentTextFieldValue.text.isNotBlank()
        ) {
            Icon(
                imageVector = when (replyMode) {
                    is ReplyMode.EditMessage -> Icons.Default.Edit
                    is ReplyMode.NewMessage -> Icons.AutoMirrored.Filled.Send
                },
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CancelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
            onFileSelected = { _ -> }
        )
    }
}
