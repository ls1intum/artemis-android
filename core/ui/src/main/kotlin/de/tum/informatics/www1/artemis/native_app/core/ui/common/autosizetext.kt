package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * From: https://stackoverflow.com/a/69780826/5608927
 * By: Rahul Sainani; Adapted from Brian who pasted his answer here: https://stackoverflow.com/a/66090448/5608927
 * Further adapted by Tim Ortel
 *
 * Posted on StackOverflow under this license: https://creativecommons.org/licenses/by-sa/4.0/
 */
@Composable
fun AutoResizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    // Changes by Tim Ortel: use a box with constraints to automatically start a reevaluation when the bounding
    // box of this text changes.
    BoxWithConstraints(modifier) {
        var fontSizeValue by remember(maxWidth, maxHeight) { mutableStateOf(fontSizeRange.max.value) }
        var readyToDraw by remember(maxWidth, maxHeight) { mutableStateOf(false) }

        Text(
            text = text,
            color = color,
            maxLines = maxLines,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            style = style,
            fontSize = fontSizeValue.sp,
            onTextLayout = {
                if (it.didOverflowHeight && !readyToDraw) {
                    val nextFontSizeValue = fontSizeValue - fontSizeRange.step.value
                    if (nextFontSizeValue <= fontSizeRange.min.value) {
                        // Reached minimum, set minimum font size and it's readToDraw
                        fontSizeValue = fontSizeRange.min.value
                        readyToDraw = true
                    } else {
                        // Text doesn't fit yet and haven't reached minimum text range, keep decreasing
                        fontSizeValue = nextFontSizeValue
                    }
                } else {
                    // Text fits before reaching the minimum, it's readyToDraw
                    readyToDraw = true
                }
            },
            modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() }
        )
    }

}

data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = DEFAULT_TEXT_STEP,
) {
    init {
        require(min < max) { "min should be less than max, $this" }
        require(step.value > 0) { "step should be greater than 0, $this" }
    }

    companion object {
        private val DEFAULT_TEXT_STEP = 1.sp
    }
}