package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import kotlin.math.ceil

@Composable
internal fun BasicMarkdownTextField(
    modifier: Modifier = Modifier,
    textFieldValue: TextFieldValue,
    hintText: AnnotatedString,
    focusRequester: FocusRequester,
    maxVisibleLines: Int = 8,
    extraHeight: Dp = 0.dp,
    maximumExpansion: Dp = 240.dp,
    onExpansionLimitChanged: (Float) -> Unit = {},
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
    val collapsedHeightLimit = if (LocalConfiguration.current.screenHeightDp < 480) 80.dp else 160.dp
    val maxHeight = with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.lineHeight.toDp() * maxVisibleLines }
        .coerceIn(48.dp, collapsedHeightLimit)
    BoxWithConstraints(modifier) {
        val naturalHeight = with(LocalDensity.current) {
            (layout?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f).toDp()
        }
            .coerceIn(48.dp, maxHeight)
        val availableExpansion = if (constraints.hasBoundedHeight) {
            (this.maxHeight - naturalHeight).coerceIn(0.dp, maximumExpansion)
        } else maximumExpansion
        SideEffect { onExpansionLimitChanged(availableExpansion.value) }
        val expansion = extraHeight.coerceIn(0.dp, availableExpansion)
        // Explicit at zero too: a previous viewport minimum must not latch an
        // expanded height when animation is disabled or the session changes.
        val heightModifier = Modifier.height(naturalHeight + expansion)
        Box(modifier = heightModifier.onSizeChanged { viewportHeight = it.height }.verticalScroll(scroll)) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = with(LocalDensity.current) { viewportHeight.toDp() }.coerceAtLeast(48.dp))
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
                    Box {
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
}
