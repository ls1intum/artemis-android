package de.tum.informatics.www1.artemis.native_app.core.ui.alert

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

private val destructiveButtonFontColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF4F4F) else Color(0xFF800000)

@Composable
fun TextAlertDialog(
    title: String?,
    text: String?,
    confirmButtonText: String?,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    TextAlertImpl(
        title = title,
        text = text?.let { { Text(text = text) } },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun MarkdownTextAlertDialog(
    title: String?,
    text: String?,
    confirmButtonText: String,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    TextAlertImpl(
        title = title,
        text = text?.let { { MarkdownText(markdown = text) } },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun DestructiveTextAlertDialog(
    title: String?,
    text: String?,
    confirmButtonText: String,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    TextAlertImpl(
        title = title,
        text = text?.let { { MarkdownText(markdown = text) } },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest,
        confirmButtonFontColor = destructiveButtonFontColor
    )
}

@Composable
fun DestructiveMarkdownTextAlertDialog(
    title: String?,
    text: String?,
    confirmButtonText: String,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    TextAlertImpl(
        title = title,
        text = text?.let { { MarkdownText(markdown = text) } },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest,
        confirmButtonFontColor = destructiveButtonFontColor
    )
}

@Composable
private fun TextAlertImpl(
    title: String?,
    text: (@Composable () -> Unit)?,
    confirmButtonText: String?,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmButtonFontColor: Color? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title?.let { { Text(text = title) } },
        text = text,
        confirmButton = confirmButtonText?.let {
            {
                TextButton(onClick = onPressPositiveButton) {
                    val color = confirmButtonFontColor ?: LocalTextStyle.current.color

                    Text(
                        text = it,
                        color = color
                    )
                }
            }
        } ?: {},
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = onDismissRequest) {
                    Text(text = dismissButtonText)
                }
            }
        }
    )
}