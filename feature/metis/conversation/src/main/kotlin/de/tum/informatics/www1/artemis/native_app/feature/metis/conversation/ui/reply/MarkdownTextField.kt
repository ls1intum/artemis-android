package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import kotlinx.coroutines.launch


const val TEST_TAG_MARKDOWN_TEXTFIELD = "TEST_TAG_MARKDOWN_TEXTFIELD"

/**
 * @param sendButton composable centered vertically right to the text field.
 */
@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    focusRequester: FocusRequester = remember { FocusRequester() },
    sendButton: @Composable () -> Unit = {},
    topRightButton: @Composable RowScope.() -> Unit = {},
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit,
    showAutoCompletePopup: ((AutocompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable () -> Unit = {},
) {
    val text = textFieldValue.text
    var selectedType by remember { mutableStateOf(ViewType.TEXT) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        focusRequester = focusRequester,
                        onFocusAcquired = onFocusAcquired,
                        onFocusLost = onFocusLost
                    )
                }

                ViewType.PREVIEW -> {
                    MarkdownText(
                        markdown = text,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            sendButton()

            topRightButton()
        }

        TextFieldOptions(
            selectedType = selectedType,
            isPreviewEnabled = text.isNotBlank(),
            showFormattingOptions = selectedType == ViewType.TEXT,
            onChangeViewType = { selectedType = it },
            formattingOptionButtons = formattingOptionButtons,
            showAutoCompletePopup = showAutoCompletePopup,
            onOpenImagePicker = { filePickerLauncher.launch("image/*") },
            onOpenFilePicker = { filePickerLauncher.launch("*/*") },
        )
    }
}

@Composable
fun BasicMarkdownTextField(
    modifier: Modifier = Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    focusRequester: FocusRequester,
    maxVisibleLines: Int = 8,
    onTextChanged: (TextFieldValue) -> Unit,
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {}
) {
    var hadFocus by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val localDensity = LocalDensity.current
    val localTextStyle = LocalTextStyle.current
    val lineHeight = with(localDensity) { localTextStyle.lineHeight.toPx() }

    LaunchedEffect(textFieldValue.selection) {
        val cursorLine = textFieldValue.text.take(textFieldValue.selection.start)
            .count { it == '\n' }
        val cursorOffset = (cursorLine * lineHeight).toInt()
        coroutineScope.launch {
            scrollState.animateScrollTo(cursorOffset)
        }
    }

    Box(
        modifier = modifier
            .heightIn(max = (localTextStyle.fontSize.value * maxVisibleLines).dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        BasicTextField(
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.hasFocus) {
                        hadFocus = true
                        onFocusAcquired()
                    }
                    if (!focusState.hasFocus && hadFocus) {
                        onFocusLost()
                        hadFocus = false
                    }
                }
                .testTag(TEST_TAG_MARKDOWN_TEXTFIELD),
            value = textFieldValue,
            onValueChange = onTextChanged,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart),
                            text = hintText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = localTextStyle,
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = localTextStyle.copy(color = MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun TextFieldOptions(
    modifier: Modifier = Modifier,
    selectedType: ViewType,
    isPreviewEnabled: Boolean,
    showFormattingOptions: Boolean,
    onChangeViewType: (ViewType) -> Unit,
    showAutoCompletePopup: ((AutocompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable () -> Unit = {},
    onOpenFilePicker: () -> Unit = {},
    onOpenImagePicker: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val offsetY by animateDpAsState(targetValue = if (expanded) 0.dp else 200.dp)
    var isTaggingDropdownExpanded by remember { mutableStateOf(false) }

    val iconButtonModifier = Modifier
        .clip(CircleShape)
        .size(32.dp)
        .background(MaterialTheme.colorScheme.surfaceContainer)
    val iconModifier = Modifier.padding(6.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showFormattingOptions) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        modifier = iconButtonModifier,
                        onClick = onOpenImagePicker
                    ) {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(id = R.drawable.image),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        modifier = iconButtonModifier,
                        onClick = onOpenFilePicker
                    ) {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(id = R.drawable.attachment),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        modifier = iconButtonModifier,
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(id = R.drawable.format_text),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        modifier = iconButtonModifier,
                        onClick = { isTaggingDropdownExpanded = !isTaggingDropdownExpanded }
                    ) {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(id = R.drawable.tag),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = isTaggingDropdownExpanded,
                    onDismissRequest = { isTaggingDropdownExpanded = false },
                    properties = PopupProperties(
                        focusable = false
                    )
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.tag),
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(R.string.reply_format_mention_members)) },
                        onClick = {
                            isTaggingDropdownExpanded = false
                            showAutoCompletePopup?.invoke(AutocompleteType.USERS)
                        }
                    )

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.reply_format_mention_channels)) },
                        onClick = {
                            isTaggingDropdownExpanded = false
                            showAutoCompletePopup?.invoke(AutocompleteType.CHANNELS)
                        }
                    )

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.reply_format_mention_exercises)) },
                        onClick = {
                            isTaggingDropdownExpanded = false
                            showAutoCompletePopup?.invoke(AutocompleteType.EXERCISES)
                        }
                    )

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.reply_format_mention_lectures)) },
                        onClick = {
                            isTaggingDropdownExpanded = false
                            showAutoCompletePopup?.invoke(AutocompleteType.LECTURES)
                        }
                    )

                }

                Box(
                    modifier = Modifier
                        .offset(y = offsetY)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            modifier = iconButtonModifier,
                            onClick = { expanded = false }
                        ) {
                            Icon(
                                modifier = iconModifier,
                                imageVector = Icons.Default.Clear,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null
                            )
                        }

                        formattingOptionButtons()
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputChip(
                selected = selectedType == ViewType.TEXT,
                onClick = { onChangeViewType(ViewType.TEXT) },
                label = {
                    Text(text = stringResource(id = R.string.markdown_textfield_tab_text))
                }
            )

            InputChip(
                selected = selectedType == ViewType.PREVIEW,
                onClick = { onChangeViewType(ViewType.PREVIEW) },
                enabled = isPreviewEnabled,
                label = {
                    Text(text = stringResource(id = R.string.markdown_textfield_tab_preview))
                }
            )
        }
    }
}

private enum class ViewType {
    TEXT,
    PREVIEW
}