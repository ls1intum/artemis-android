package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicArtemisTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.toPainter
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.appendAtCursor
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.EmojiPickerModalBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider


const val TEST_TAG_MARKDOWN_TEXTFIELD = "TEST_TAG_MARKDOWN_TEXTFIELD"
val textFormattingOptionsHiddenOffsetY = 200.dp


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    alignOptionsAtBottom: Boolean = false,
    isEmojiPickerEnabled: Boolean = true,
    isTextOptionsInitiallyVisible: Boolean = true,
    backgroundColor: Color,
    textOptionsColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    focusRequester: FocusRequester = remember { FocusRequester() },
    textFieldTrailingContent: @Composable RowScope.() -> Unit = {},
    textOptionsTopContent: @Composable ColumnScope.() -> Unit = {},
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable (Color) -> Unit = {},
) {
    val text = textFieldValue.text
    var selectedType by remember { mutableStateOf(ViewType.TEXT) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isTextOptionsVisible by remember { mutableStateOf(isTextOptionsInitiallyVisible) }
    // weight(1f) is needed for the row to ensure that the TextFieldOptions stay visible when the BasicMarkdownTextField grows.
    // fill has to be disabled for the UI tests to work properly.
    val content: @Composable ColumnScope.(applyWeight: Boolean) -> Unit = { applyWeight ->
        Row(
            modifier = Modifier
                .then(if (applyWeight) Modifier.weight(1f, false) else Modifier)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedType) {
                ViewType.TEXT -> {
                    BasicMarkdownTextField(
                        modifier = Modifier.weight(1f),
                        textFieldValue = textFieldValue,
                        onTextChanged = onTextChanged,
                        hintText = hintText,
                        backgroundColor = backgroundColor,
                        focusRequester = focusRequester,
                        onFocusAcquired = {
                            onFocusAcquired()
                            isTextOptionsVisible = true
                        },
                        onFocusLost = {
                            onFocusLost()
                            isTextOptionsVisible = false
                        }
                    )
                }

                ViewType.PREVIEW -> {
                    MarkdownText(
                        markdown = text,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            textFieldTrailingContent()
        }
    }

val options: @Composable (applyPadding: Boolean) -> Unit = { applyPadding ->
        TextFieldOptions(
            modifier = Modifier.then(if (applyPadding) Modifier.padding(top = 4.dp) else Modifier),
            selectedType = selectedType,
            isPreviewEnabled = text.isNotBlank(),
            isEmojiPickerEnabled = isEmojiPickerEnabled,
            backgroundColor = backgroundColor,
            containerColor = textOptionsColor,
            showFormattingOptions = selectedType == ViewType.TEXT,
            onChangeViewType = { selectedType = it },
            formattingOptionButtons = formattingOptionButtons,
            showAutoCompletePopup = showAutoCompletePopup,
            onOpenImagePicker = { filePickerLauncher.launch("image/*") },
            onOpenFilePicker = { filePickerLauncher.launch("*/*") },
            onOpenEmojiPicker = { showEmojiPicker = true }
            )
    }

    if (alignOptionsAtBottom) {
        BoxedMarkdownTextField(
            modifier = modifier,
            backgroundColor = backgroundColor,
            isTextOptionsVisible = isTextOptionsVisible,
            content = content,
            textOptionsTopContent = textOptionsTopContent,
            options = options
        )
        return
    }

    ChatMarkdownTextField(
        modifier = modifier,
        content = content,
        textOptionsTopContent = textOptionsTopContent,
        isTextOptionsVisible = isTextOptionsVisible,
        options = options
    )

    if (showEmojiPicker) {
        EmojiPickerModalBottomSheet(
            onEmojiClicked = {
                onTextChanged(
                    textFieldValue.appendAtCursor(it.unicode)
                )
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

@Composable
private fun BoxedMarkdownTextField(
    modifier: Modifier,
    backgroundColor: Color,
    isTextOptionsVisible: Boolean,
    content: @Composable ColumnScope.(applyWeight: Boolean) -> Unit,
    textOptionsTopContent: @Composable ColumnScope.() -> Unit,
    options: @Composable (Boolean) -> Unit,
) {
    Box(
        modifier = modifier.imePadding()
    ) {
        val scrollState = rememberScrollState()
        val optionsHeight = if (isTextOptionsVisible) 56.dp else 0.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = optionsHeight)
        ) {
            content(false)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                content = textOptionsTopContent
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(backgroundColor)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(isTextOptionsVisible) {
                options(true)
            }
        }
    }
}

@Composable
private fun ChatMarkdownTextField(
    modifier: Modifier,
    content: @Composable ColumnScope.(applyWeight: Boolean) -> Unit,
    textOptionsTopContent: @Composable ColumnScope.() -> Unit,
    isTextOptionsVisible: Boolean = true,
    options: @Composable (Boolean) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content(true)
        textOptionsTopContent()
        AnimatedVisibility(isTextOptionsVisible) {
            options(false)
        }
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
    onTextChanged: (TextFieldValue) -> Unit,
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {}
) {
    var hadFocus by remember { mutableStateOf(false) }
    val localTextStyle = LocalTextStyle.current

    BasicArtemisTextField(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = (localTextStyle.fontSize.value * maxVisibleLines).dp)
            .testTag(TEST_TAG_MARKDOWN_TEXTFIELD),
        backgroundColor = backgroundColor,
        value = textFieldValue,
        onValueChange = onTextChanged,
        focusRequester = focusRequester,
        onFocusChanged = { hasFocus ->
            if (hasFocus) {
                hadFocus = true
                onFocusAcquired()
            }
            if (!hasFocus && hadFocus) {
                onFocusLost()
                hadFocus = false
            }
        },
        hint = hintText.text
    )
}

@Composable
private fun TextFieldOptions(
    modifier: Modifier = Modifier,
    selectedType: ViewType,
    isPreviewEnabled: Boolean,
    isEmojiPickerEnabled: Boolean,
    backgroundColor: Color,
    containerColor: Color,
    showFormattingOptions: Boolean,
    onChangeViewType: (ViewType) -> Unit,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable (Color) -> Unit = {},
    onOpenFilePicker: () -> Unit = {},
    onOpenImagePicker: () -> Unit = {},
    onOpenEmojiPicker: () -> Unit = {}
) {
    var isTextFormattingExpanded by remember { mutableStateOf(false) }
    val textFormattingOptionsOffsetY by animateDpAsState(targetValue = if (isTextFormattingExpanded) 0.dp else textFormattingOptionsHiddenOffsetY)
    var isTaggingDropdownExpanded by remember { mutableStateOf(false) }

    val textOptionsFieldOptionsModifier = Modifier
        .clip(CircleShape)
        .background(containerColor)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showFormattingOptions) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextFieldOptionsIconButton(
                        modifier = textOptionsFieldOptionsModifier,
                        painter = painterResource(id = R.drawable.image),
                        containerColor = containerColor
                    ) {
                        onOpenImagePicker()
                    }

                    TextFieldOptionsIconButton(
                        modifier = textOptionsFieldOptionsModifier,
                        painter = painterResource(id = R.drawable.attachment),
                        containerColor = containerColor
                    ) {
                        onOpenFilePicker()
                    }

                    TextFieldOptionsIconButton(
                        modifier = textOptionsFieldOptionsModifier,
                        painter = painterResource(id = R.drawable.format_text),
                        containerColor = containerColor
                    ) {
                        isTextFormattingExpanded = !isTextFormattingExpanded
                    }

                    if (isEmojiPickerEnabled) {
                        TextFieldOptionsIconButton(
                            painter = Icons.Outlined.AddReaction.toPainter(),
                            containerColor = containerColor
                        ) {
                            onOpenEmojiPicker()
                        }
                    }

                    TextFieldOptionsIconButton(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.tag),
                        containerColor = containerColor
                    ) {
                        isTaggingDropdownExpanded = !isTaggingDropdownExpanded
                    }
                }

                TaggingDropdownMenu(
                    isTaggingDropdownExpanded = isTaggingDropdownExpanded,
                    showAutoCompletePopup = showAutoCompletePopup,
                    onDismissRequest = { isTaggingDropdownExpanded = false }
                )

                Box(
                    modifier = modifier
                        .offset(y = textFormattingOptionsOffsetY)
                        .background(backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextFieldOptionsIconButton(
                            imageVector = Icons.Default.Clear,
                            containerColor = containerColor
                        ) {
                            isTextFormattingExpanded = false
                        }

                        formattingOptionButtons(containerColor)
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        PreviewEditRow(
            modifier = modifier,
            selectedType = selectedType,
            isPreviewEnabled = isPreviewEnabled,
            onChangeViewType = onChangeViewType
        )
    }
}

@Composable
private fun TextFieldOptionsIconButton(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    containerColor: Color,
    onClick: () -> Unit
) {
    require(painter != null || imageVector != null)

    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .size(32.dp)
            .background(containerColor),
        onClick = onClick
    ) {
        painter?.let {
            Icon(
                modifier = Modifier.padding(6.dp),
                painter = painter,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
            return@IconButton
        }

        imageVector?.let {
            Icon(
                modifier = Modifier.padding(6.dp),
                imageVector = it,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TaggingDropdownMenu(
    isTaggingDropdownExpanded: Boolean,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val isFaqEnabled = LocalReplyAutoCompleteHintProvider.current.isFaqEnabled

    val onClick: (AutoCompleteType) -> Unit = { autoCompleteType ->
        showAutoCompletePopup?.invoke(autoCompleteType)
        onDismissRequest()
    }

    DropdownMenu(
        expanded = isTaggingDropdownExpanded,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false
        )
    ) {
        TaggingDropDownMenuItem(
            iconPainter = painterResource(id = R.drawable.tag),
            text = stringResource(R.string.reply_format_mention_members),
            onClick = { onClick(AutoCompleteType.USERS) }
        )

        TaggingDropDownMenuItem(
            iconPainter = Icons.Default.Numbers.toPainter(),
            text = stringResource(R.string.reply_format_mention_channels),
            onClick = { onClick(AutoCompleteType.CHANNELS) }
        )

        TaggingDropDownMenuItem(
            iconPainter = Icons.AutoMirrored.Filled.ListAlt.toPainter(),
            text = stringResource(R.string.reply_format_mention_exercises),
            onClick = { onClick(AutoCompleteType.EXERCISES) }
        )

        TaggingDropDownMenuItem(
            iconPainter = Icons.Default.School.toPainter(),
            text = stringResource(R.string.reply_format_mention_lectures),
            onClick = { onClick(AutoCompleteType.LECTURES) }
        )

        if (isFaqEnabled) {
            TaggingDropDownMenuItem(
                iconPainter = Icons.Default.QuestionMark.toPainter(),
                text = stringResource(R.string.reply_format_mention_faqs),
                onClick = { onClick(AutoCompleteType.FAQS) }
            )
        }
    }
}

@Composable
private fun TaggingDropDownMenuItem(
    modifier: Modifier = Modifier,
    iconPainter: Painter,
    text: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = iconPainter,
                contentDescription = text
            )
        },
        text = { Text(text) },
        onClick = onClick
    )
}


@Composable
private fun PreviewEditRow(
    modifier: Modifier = Modifier,
    selectedType: ViewType,
    isPreviewEnabled: Boolean = false,
    onChangeViewType: (ViewType) -> Unit = {}
) {
    Row(
        modifier = modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InputChip(
            selected = selectedType == ViewType.TEXT,
            onClick = { onChangeViewType(ViewType.TEXT) },
            label = {
                Text(text = stringResource(id = R.string.markdown_textfield_tab_text))
            },
            border = InputChipDefaults.inputChipBorder(
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                enabled = true,
                selected = selectedType == ViewType.TEXT,
            )
        )

        InputChip(
            selected = selectedType == ViewType.PREVIEW,
            onClick = { onChangeViewType(ViewType.PREVIEW) },
            enabled = isPreviewEnabled,
            label = {
                Text(text = stringResource(id = R.string.markdown_textfield_tab_preview))
            },
            border = InputChipDefaults.inputChipBorder(
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                enabled = isPreviewEnabled,
                selected = selectedType == ViewType.PREVIEW,
            )
        )
    }
}

private enum class ViewType {
    TEXT,
    PREVIEW
}