package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

/**
 * Display a clickable text with button that shows a dialog on click.
 */
@Composable
fun DropdownButton(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    dialogContent: @Composable ColumnScope.(hide: () -> Unit) -> Unit
) {
    var displayDialog by remember {
        mutableStateOf(false)
    }

    OutlinedButton(
        modifier = modifier,
        onClick = { displayDialog = true }
    ) {
        var displayText by remember { mutableStateOf(true) }

        if (displayText) {
            Text(
                modifier = Modifier.weight(1f, false),
                text = text,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = {
                    displayText = !it.hasVisualOverflow
                },
                maxLines = 1
            )
        } else {
            Icon(imageVector = icon, contentDescription = null)
        }

        Icon(
            modifier = Modifier,
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null
        )

        DropdownMenu(expanded = displayDialog, onDismissRequest = { displayDialog = false }) {
            dialogContent { displayDialog = false }
        }
    }
}