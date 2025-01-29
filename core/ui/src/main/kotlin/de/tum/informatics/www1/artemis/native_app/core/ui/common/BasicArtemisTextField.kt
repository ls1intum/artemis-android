package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
    val keyboardController = LocalSoftwareKeyboardController.current

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
        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
        cursorBrush = SolidColor(LocalContentColor.current),
        visualTransformation = { text ->
            if (isValueDisplayed) TransformedText(text, OffsetMapping.Identity)
            else TransformedText(
                AnnotatedString(text = currentValue, spanStyle = hintStyle.toSpanStyle()),
                OffsetMapping.Identity
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}

@Composable
fun BasicArtemisTextField(
    modifier: Modifier,
    value: TextFieldValue,
    hint: String,
    leadingHintIcon: ImageVector? = null,
    maxLines: Int = Int.MAX_VALUE,
    focusRequester: FocusRequester? = null,
    hideHintOnFocus: Boolean = false,
    backgroundColor: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onFocusChanged: ((Boolean) -> Unit),
    onValueChange: (TextFieldValue) -> Unit,
) {
    val localTextStyle = LocalTextStyle.current

    BasicTextField(
        modifier = modifier
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.hasFocus)
            }
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
        value = value,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(backgroundColor)
            ) {
                if (value.text.isEmpty() && !hideHintOnFocus) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        leadingHintIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = hint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = localTextStyle,
                        )
                    }
                }
                innerTextField()
            }
        },
        maxLines = maxLines,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = localTextStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
fun BasicSearchTextField(
    modifier: Modifier,
    hint: String,
    query: String,
    updateQuery: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember { mutableStateOf(TextFieldValue(query)) }

    LaunchedEffect(query) {
        if (query != textFieldValue.text) {
            textFieldValue = TextFieldValue(query, textFieldValue.selection)
        }
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            BasicArtemisTextField(
                modifier = Modifier
                    .weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                hint = hint,
                leadingHintIcon = Icons.Default.Search,
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    updateQuery(newValue.text)
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                onFocusChanged = {}
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { updateQuery("") },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}