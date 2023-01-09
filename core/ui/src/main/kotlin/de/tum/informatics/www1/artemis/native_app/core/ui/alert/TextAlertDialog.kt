package de.tum.informatics.www1.artemis.native_app.core.ui.alert

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

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
private fun TextAlertImpl(
    title: String?,
    text: (@Composable () -> Unit)?,
    confirmButtonText: String?,
    dismissButtonText: String?,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title?.let { { Text(text = title) } },
        text = text,
        confirmButton = confirmButtonText?.let {
            {
                TextButton(onClick = onPressPositiveButton) {
                    Text(text = it)
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