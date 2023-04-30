package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText

@Composable
fun BasicHintTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    maxLines: Int,
    hideHintOnFocus: Boolean = true,
    hintStyle: TextStyle = LocalTextStyle.current
) {
    var hasFocus by remember { mutableStateOf(false) }


    val isValueDisplayed = value.isNotBlank() || (hasFocus && hideHintOnFocus)
    val currentValue = if (isValueDisplayed) value else hint

    BasicTextField(
        modifier = modifier
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
            },
        value = currentValue,
        onValueChange = onValueChange,
        maxLines = maxLines,
        visualTransformation = { text ->
            if (isValueDisplayed) TransformedText(text, OffsetMapping.Identity)
            else TransformedText(
                AnnotatedString(text = currentValue, spanStyle = hintStyle.toSpanStyle()),
                OffsetMapping.Identity
            )
        },
    )
}