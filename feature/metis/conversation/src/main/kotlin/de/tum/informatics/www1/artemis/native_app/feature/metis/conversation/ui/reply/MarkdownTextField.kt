package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.EmojiPickerModalBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.ComposerHistory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownAttachments
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownDocument
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil

private val ComposerResizeSaver = Saver<ComposerResizeState, Float>(
    save = { it.extraHeight }, restore = { ComposerResizeState(it) },
)

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
    var resize by rememberSaveable(editorSession, stateSaver = ComposerResizeSaver) { mutableStateOf(ComposerResizeState()) }
    var isDragging by remember(editorSession) { mutableStateOf(false) }
    val maximumExpansion = if (LocalConfiguration.current.screenHeightDp < 480) 120f else 240f
    var availableExpansion by remember(editorSession) { mutableFloatStateOf(maximumExpansion) }
    val expansionLimit = minOf(maximumExpansion, availableExpansion)
    val targetExpansion = resize.extraHeight.coerceIn(0f, expansionLimit)
    val renderedExpansion by key(editorSession) {
        animateFloatAsState(
            targetValue = targetExpansion,
            animationSpec = if (isDragging) snap() else tween(220),
            label = "Composer expansion",
        )
    }
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

    val attachments = remember(textFieldValue.text) { MarkdownAttachments.list(textFieldValue.text) }
    BoxWithConstraints(modifier = modifier.then(if (alignOptionsAtBottom) Modifier.imePadding() else Modifier)) {
        // Keep conversation/completion context above an expanded inline composer.
        // Preview tiles may occupy up to 96 dp at large text sizes. In very
        // short windows the inline attachment stays editable; its preview yields
        // space to the editor and Send.
        val showAttachmentPreviews = !constraints.hasBoundedHeight || maxHeight >= 120.dp + ComposerAttachmentPreviewMaxHeight
        val attachmentBudget = if (showAttachmentPreviews && attachments.isNotEmpty()) ComposerAttachmentPreviewMaxHeight else 0.dp
        // Three 48 dp targets (editor, grabber, toolbar) plus 24 dp padding.
        val minimumResizableHeight = 168.dp + attachmentBudget
        val surfaceBudget = if (constraints.hasBoundedHeight) {
            maxHeight - (maxHeight - minimumResizableHeight).coerceIn(0.dp, 96.dp)
        } else maxHeight
        val showResizeHandle = (!constraints.hasBoundedHeight || surfaceBudget >= minimumResizableHeight) &&
            (attachments.isEmpty() || showAttachmentPreviews)
        Column {
            if (alignOptionsAtBottom) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = textOptionsTopContent)
            }
            Surface(
                modifier = Modifier.heightIn(max = surfaceBudget),
                shape = RoundedCornerShape(24.dp),
                color = backgroundColor.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            ) {
                Column(Modifier.padding(8.dp)) {
                    if (showResizeHandle) {
                        key(editorSession) {
                            ComposerResizeHandle(
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                                expanded = targetExpansion > 0f,
                                maximumExpansion = expansionLimit,
                                onToggle = { resize = resize.setExpanded(targetExpansion == 0f, expansionLimit) },
                                onExpand = { resize = resize.setExpanded(true, expansionLimit) },
                                onCollapse = { resize = resize.setExpanded(false, expansionLimit) },
                                onDragStart = {
                                    resize = resize.beginGesture(renderedExpansion, expansionLimit)
                                    isDragging = true
                                },
                                onDrag = { resize = resize.update(it, expansionLimit) },
                                onDragEnd = { vertical, horizontal ->
                                    resize = resize.finish(vertical, horizontal, expansionLimit)
                                    isDragging = false
                                },
                                onDragCancel = { resize = resize.cancelGesture(); isDragging = false },
                            )
                        }
                    }
                    key(editorSession) {
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
                                        extraHeight = if (showResizeHandle) renderedExpansion.dp else 0.dp,
                                    maximumExpansion = maximumExpansion.dp,
                                    onExpansionLimitChanged = { availableExpansion = it },
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
                    }
                    if (showAttachmentPreviews) {
                        ComposerAttachments(value = textFieldValue, onTextChanged = commit, attachments = attachments)
                    }
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

