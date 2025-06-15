package de.tum.informatics.www1.artemis.native_app.core.ui.alert

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

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
        confirmButtonFontColor = ComponentColors.TextAlertDialog.destructiveButtonFont
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
        confirmButtonFontColor = ComponentColors.TextAlertDialog.destructiveButtonFont
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
}