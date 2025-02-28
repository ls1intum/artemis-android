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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QuestionMark
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicArtemisTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import kotlinx.coroutines.launch


const val TEST_TAG_MARKDOWN_TEXTFIELD = "TEST_TAG_MARKDOWN_TEXTFIELD"
val textFormattingOptionsHiddenOffsetY = 200.dp


@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    focusRequester: FocusRequester = remember { FocusRequester() },
    textFieldTrailingContent: @Composable RowScope.() -> Unit = {},
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable () -> Unit = {},
) {
    val text = textFieldValue.text
    var selectedType by remember { mutableStateOf(ViewType.TEXT) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // weight(1f) is needed for the row to ensure that the TextFieldOptions stay visible when the BasicMarkdownTextField grows.
        // fill has to be disabled for the UI tests to work properly.
        Row(
            modifier = Modifier
                .weight(1f, false)
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

            textFieldTrailingContent()
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

    // This check is needed for the UI Test to explicitly set the font size if it is unspecified
    val lineHeight = with(localDensity) {
        if (localTextStyle.lineHeight.isUnspecified) {
            16.sp.toPx()
        } else {
            localTextStyle.lineHeight.toPx()
        }
    }

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
            .verticalScroll(scrollState)
    ) {
        BasicArtemisTextField(
            modifier = modifier
                .fillMaxWidth()
                .testTag(TEST_TAG_MARKDOWN_TEXTFIELD),
            backgroundColor = MaterialTheme.colorScheme.background,
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
}

@Composable
private fun TextFieldOptions(
    modifier: Modifier = Modifier,
    selectedType: ViewType,
    isPreviewEnabled: Boolean,
    showFormattingOptions: Boolean,
    onChangeViewType: (ViewType) -> Unit,
    showAutoCompletePopup: ((AutoCompleteType) -> Unit)? = null,
    formattingOptionButtons: @Composable () -> Unit = {},
    onOpenFilePicker: () -> Unit = {},
    onOpenImagePicker: () -> Unit = {}
) {
    var isTextFormattingExpanded by remember { mutableStateOf(false) }
    val textFormattingOptionsOffsetY by animateDpAsState(targetValue = if (isTextFormattingExpanded) 0.dp else textFormattingOptionsHiddenOffsetY)
    var isTaggingDropdownExpanded by remember { mutableStateOf(false) }

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
                    TextFieldOptionsIconButton(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.image),
                    ) {
                        onOpenImagePicker()
                    }

                    TextFieldOptionsIconButton(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.attachment)
                    ) {
                        onOpenFilePicker()
                    }

                    TextFieldOptionsIconButton(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.format_text)
                    ) {
                        isTextFormattingExpanded = !isTextFormattingExpanded
                    }

                    TextFieldOptionsIconButton(
                        modifier = Modifier,
                        painter = painterResource(id = R.drawable.tag)
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
                    modifier = Modifier
                        .offset(y = textFormattingOptionsOffsetY)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextFieldOptionsIconButton(
                            modifier = Modifier,
                            imageVector = Icons.Default.Clear
                        ) {
                            isTextFormattingExpanded = false
                        }

                        formattingOptionButtons()
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        PreviewEditRow(
            modifier = Modifier,
            selectedType = selectedType,
            isPreviewEnabled = isPreviewEnabled,
            onChangeViewType = onChangeViewType
        )
    }
}

@Composable
private fun TextFieldOptionsIconButton(
    modifier: Modifier,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    onClick: () -> Unit
) {
    require(painter != null || imageVector != null)

    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer),
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
            painter = painterResource(id = R.drawable.tag),
            text = stringResource(R.string.reply_format_mention_members),
            onClick = { onClick(AutoCompleteType.USERS) }
        )

        TaggingDropDownMenuItem(
            imageVector = Icons.Default.Tag,
            text = stringResource(R.string.reply_format_mention_channels),
            onClick = { onClick(AutoCompleteType.CHANNELS) }
        )

        TaggingDropDownMenuItem(
            imageVector = Icons.AutoMirrored.Filled.ListAlt,
            text = stringResource(R.string.reply_format_mention_exercises),
            onClick = { onClick(AutoCompleteType.EXERCISES) }
        )

        TaggingDropDownMenuItem(
            imageVector = Icons.Default.School,
            text = stringResource(R.string.reply_format_mention_lectures),
            onClick = { onClick(AutoCompleteType.LECTURES) }
        )

        TaggingDropDownMenuItem(
            imageVector = Icons.Default.QuestionMark,
            text = stringResource(R.string.reply_format_mention_faqs),
            onClick = { onClick(AutoCompleteType.FAQS) }
        )
    }
}

@Composable
private fun TaggingDropDownMenuItem(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    text: String,
    onClick: () -> Unit
) {
    require(painter != null || imageVector != null)

    val iconPainter = painter ?: rememberVectorPainter(imageVector!!)

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
    modifier: Modifier,
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

private enum class ViewType {
    TEXT,
    PREVIEW
}