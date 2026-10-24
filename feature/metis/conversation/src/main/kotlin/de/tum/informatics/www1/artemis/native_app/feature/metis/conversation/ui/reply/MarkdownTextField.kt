package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.OffsetMapping
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownDocument
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.ComposerHistory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.appendAtCursor
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.EmojiPickerModalBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil
import kotlin.math.ceil

const val TEST_TAG_MARKDOWN_TEXTFIELD = "TEST_TAG_MARKDOWN_TEXTFIELD"

/** Shared native composer for messages, replies, edits and forwarding. */
@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    alignOptionsAtBottom: Boolean = false,
    isEmojiPickerEnabled: Boolean = true,
    editorSession: Any? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onPrepareAttachment: () -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() },
    textFieldTrailingContent: @Composable RowScope.() -> Unit = {},
    textOptionsTopContent: @Composable ColumnScope.() -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)? = null,
) {
    var typingMarkNames by rememberSaveable(editorSession) { mutableStateOf<List<String>?>(null) }
    val typingMarks = typingMarkNames?.map { MarkdownStyle.valueOf(it) }?.toSet()
    val history = remember(editorSession) { ComposerHistory() }
    SideEffect { history.observe(textFieldValue) }
    val commit: (TextFieldValue) -> Unit = { next -> history.commit(textFieldValue, next); onTextChanged(next) }
    val undo = { typingMarkNames = null; history.undo()?.let(onTextChanged); Unit }
    val redo = { typingMarkNames = null; history.redo()?.let(onTextChanged); Unit }
    var formattingExpanded by rememberSaveable { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var linkSelection by remember { mutableStateOf<TextFieldValue?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val nativeClipboard = LocalClipboard.current
    val currentDocument = rememberUpdatedState(textFieldValue)
    val clipboard = remember(nativeClipboard, editorSession) { ComposerClipboard(nativeClipboard) { currentDocument.value } }
    val focusEditor = {
        focusRequester.requestFocus()
        keyboard?.show()
        Unit
    }

    val capabilities = remember(textFieldValue.text, textFieldValue.selection) { composerEditingCapabilities(textFieldValue) }
    val applyStyle: (MarkdownStyle) -> Unit = { style ->
        if (style != MarkdownStyle.CodeBlock && !capabilities.inlineActions) Unit
        else if (textFieldValue.selection.collapsed && style in MarkdownDocument.inlineStyles) {
            val active = typingMarks ?: MarkdownEditEngine.activeMarks(textFieldValue).intersect(MarkdownDocument.inlineStyles)
            typingMarkNames = (if (style in active) active - style else active + style).map { it.name }
        } else {
            if (style == MarkdownStyle.CodeBlock) typingMarkNames = null
            commit(MarkdownStyleUtil.toggle(style, textFieldValue))
        }
    }

    Column(modifier = modifier.then(if (alignOptionsAtBottom) Modifier.imePadding() else Modifier)) {
        if (alignOptionsAtBottom) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = textOptionsTopContent)
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = backgroundColor.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        ) {
            Column(Modifier.padding(8.dp)) {
                ComposerKeyboardInput(onBackspace = {
                    MarkdownEditEngine.removeBlockAtStart(textFieldValue)?.let { commit(it); true } ?: false
                }) {
                CompositionLocalProvider(LocalClipboard provides clipboard) {
                BasicMarkdownTextField(
                    // Measure the action row first so long drafts cannot consume Send's space.
                    modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp, vertical = 4.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace && !event.isCtrlPressed &&
                                MarkdownEditEngine.removeBlockAtStart(textFieldValue)?.let { commit(it); true } == true) true
                            else if (event.type != KeyEventType.KeyDown || !event.isCtrlPressed) false
                            else when (event.key) {
                                Key.Z -> { if (event.isShiftPressed) redo() else undo(); true }
                                Key.Y -> { redo(); true }
                                Key.B -> { applyStyle(MarkdownStyle.Bold); true }
                                Key.I -> { applyStyle(MarkdownStyle.Italic); true }
                                Key.U -> { applyStyle(MarkdownStyle.Underline); true }
                                Key.K -> { if (capabilities.link) linkSelection = textFieldValue; true }
                                else -> false
                            }
                        },
                    textFieldValue = textFieldValue,
                    hintText = hintText,
                    focusRequester = focusRequester,
                    backgroundColor = Color.Transparent,
                    isCodeInput = !capabilities.inlineActions || MarkdownStyle.InlineCode in
                        (typingMarks ?: MarkdownEditEngine.activeMarks(textFieldValue)),
                    onTextChanged = { native ->
                        val previous = MarkdownEditEngine.visibleValue(textFieldValue)
                        if (native.text == previous.text && native.selection != previous.selection) typingMarkNames = null
                        commit(clipboard.consumePaste(textFieldValue, native, typingMarks)
                            ?: MarkdownEditEngine.applyVisibleEdit(textFieldValue, native, typingMarks))
                    },
                )
                }
                }
                ComposerAttachments(value = textFieldValue, onTextChanged = commit)
                if (!alignOptionsAtBottom) textOptionsTopContent()
                ComposerToolbar(
                    textFieldValue = textFieldValue,
                    typingMarks = typingMarks,
                    capabilities = capabilities,
                    formattingExpanded = formattingExpanded,
                    isEmojiPickerEnabled = isEmojiPickerEnabled,
                    onToggleFormatting = {
                        formattingExpanded = !formattingExpanded
                        focusEditor()
                    },
                    onApplyStyle = {
                        applyStyle(it)
                        focusEditor()
                    },
                    onLink = { if (capabilities.link) linkSelection = textFieldValue },
                    onOpenImagePicker = { onPrepareAttachment(); filePickerLauncher.launch("image/*") },
                    onOpenFilePicker = { onPrepareAttachment(); filePickerLauncher.launch("*/*") },
                    onOpenEmojiPicker = { showEmojiPicker = true },
                    onMention = showAutoCompletePopup?.let { request ->
                        { type ->
                            commit(MarkdownEditEngine.insertText(textFieldValue, if (type == AutoCompleteType.USERS) "@" else "#", typingMarks))
                            focusEditor()
                            request(type)
                        }
                    },
                    canUndo = history.canUndo,
                    canRedo = history.canRedo,
                    onUndo = { undo(); focusEditor() },
                    onRedo = { redo(); focusEditor() },
                    trailingContent = textFieldTrailingContent,
                )
            }
        }
    }

    // Picker and dialog presentation belongs to the composer, so taking focus never destroys it.
    if (showEmojiPicker) {
        EmojiPickerModalBottomSheet(
            onEmojiClicked = {
                commit(MarkdownEditEngine.insertText(textFieldValue, it.unicode, typingMarks))
                showEmojiPicker = false
                focusEditor()
            },
            onDismiss = { showEmojiPicker = false; focusEditor() },
        )
    }
    linkSelection?.let { selection ->
        ComposerLinkDialog(
            value = selection,
            currentValue = textFieldValue,
            onDismiss = { linkSelection = null; focusEditor() },
            onConfirm = {
                commit(it)
                linkSelection = null
                focusEditor()
            },
        )
    }
}

@Composable
fun BasicMarkdownTextField(
    modifier: Modifier = Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    focusRequester: FocusRequester,
    backgroundColor: Color,
    maxVisibleLines: Int = 8,
    isCodeInput: Boolean = false,
    onTextChanged: (TextFieldValue) -> Unit,
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val presentation = remember(textFieldValue.text) { MarkdownPresentation.parse(textFieldValue.text) }
    val nativeValue = TextFieldValue(presentation.text,
        TextRange(presentation.offsetMapping.originalToTransformed(textFieldValue.selection.start), presentation.offsetMapping.originalToTransformed(textFieldValue.selection.end)),
        textFieldValue.composition?.let { TextRange(presentation.offsetMapping.originalToTransformed(it.start), presentation.offsetMapping.originalToTransformed(it.end)) })
    val styled = remember(presentation, colors) { composerStyledText(presentation, colors) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scroll = rememberScrollState()
    var viewportHeight by remember { mutableIntStateOf(0) }
    // The viewport can shrink without a selection change (IME, rotation, large
    // text). Keep the complete caret line visible inside our bounded scroller.
    LaunchedEffect(layout, nativeValue.selection, viewportHeight) {
        val result = layout
        if (result != null && viewportHeight > 0) {
            val caret = result.getCursorRect(nativeValue.selection.end.coerceIn(0, result.layoutInput.text.length))
            when {
                caret.bottom > scroll.value + viewportHeight -> scroll.scrollTo(ceil(caret.bottom - viewportHeight).toInt())
                caret.top < scroll.value -> scroll.scrollTo(caret.top.toInt().coerceAtLeast(0))
            }
        }
    }
    val transformation = remember(styled) {
        VisualTransformation { input ->
            TransformedText(if (input.text == styled.text) styled else input, OffsetMapping.Identity)
        }
    }
    val maxHeight = with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.lineHeight.toDp() * maxVisibleLines }
    Box(modifier = modifier.heightIn(min = 48.dp, max = maxHeight)
        .onSizeChanged { viewportHeight = it.height }.verticalScroll(scroll)) {
    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .composerBlockBackgrounds(presentation, layout, colors)
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocusAcquired() else onFocusLost() }
            .testTag(TEST_TAG_MARKDOWN_TEXTFIELD),
        value = nativeValue,
        onValueChange = onTextChanged,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = if (isCodeInput) KeyboardCapitalization.None else KeyboardCapitalization.Sentences,
            autoCorrectEnabled = if (isCodeInput) false else null,
        ),
        maxLines = Int.MAX_VALUE,
        onTextLayout = { layout = it },
        visualTransformation = transformation,
        decorationBox = { innerTextField ->
            androidx.compose.foundation.layout.Box {
                if (nativeValue.text.isEmpty()) {
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
    }
}
