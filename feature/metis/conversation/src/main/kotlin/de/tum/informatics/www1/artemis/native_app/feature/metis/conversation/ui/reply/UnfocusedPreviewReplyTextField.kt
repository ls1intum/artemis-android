package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R

private const val DisabledContentAlpha = 0.75f


@Composable
internal fun UnfocusedPreviewReplyTextField(
    modifier: Modifier = Modifier,
    onRequestShowTextField: () -> Unit,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    hintText: AnnotatedString
) {
    var isFileDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onRequestShowTextField),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledContentAlpha),
            text = hintText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(modifier = Modifier.align(Alignment.Top)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable {
                        isFileDropdownExpanded = !isFileDropdownExpanded
                    }
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(4.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background
                )
            }

            UploadDropdownMenu(
                isFileDropdownExpanded = isFileDropdownExpanded,
                onDismissRequest = { isFileDropdownExpanded = false },
                filePickerLauncher = filePickerLauncher
            )
        }
    }
}

@Composable
private fun UploadDropdownMenu(
    isFileDropdownExpanded: Boolean,
    onDismissRequest: () -> Unit,
    filePickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    DropdownMenu(
        expanded = isFileDropdownExpanded,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false
        )
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.attachment),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.reply_format_file_upload)) },
            onClick = {
                onDismissRequest()
                filePickerLauncher.launch("*/*")
            }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.reply_format_image_upload)) },
            onClick = {
                onDismissRequest()
                filePickerLauncher.launch("image/*")
            }
        )
    }
}
