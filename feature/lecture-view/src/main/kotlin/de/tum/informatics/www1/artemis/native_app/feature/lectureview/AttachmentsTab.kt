package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.RefreshableLazyColumn
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime

@Composable
internal fun AttachmentsTab(
    modifier: Modifier,
    attachments: List<Attachment>,
    onClickFileAttachment: (Attachment) -> Unit,
    onClickOpenLinkAttachment: (Attachment) -> Unit,
    onRefresh : () -> Unit
) {
    if (attachments.isNotEmpty()) {
        RefreshableLazyColumn(
            modifier = modifier,
            onRefresh = onRefresh,
            contentPadding = PaddingValues(
                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            )
        ) {
            items(attachments) { attachment ->
                AttachmentItem(
                    modifier = Modifier.fillMaxWidth(),
                    attachment = attachment,
                    onClick = {
                        when (attachment.attachmentType) {
                            Attachment.AttachmentType.FILE -> onClickFileAttachment(attachment)
                            Attachment.AttachmentType.URL -> onClickOpenLinkAttachment(attachment)
                        }
                    }
                )
            }
        }
    } else {
        Box(modifier = modifier) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
                text = stringResource(id = R.string.lecture_view_attachments_empty),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AttachmentItem(modifier: Modifier, attachment: Attachment, onClick: () -> Unit) {
    val imageVector = when (attachment.attachmentType) {
        Attachment.AttachmentType.FILE -> Icons.Default.Description
        Attachment.AttachmentType.URL -> Icons.Default.Link
    }

    val version = attachment.version
    val uploadDate = attachment.uploadDate
    val formattedUploadDate = uploadDate?.let { getRelativeTime(to = it) }

    val supportingContent = when {
        version != null && formattedUploadDate != null ->
            stringResource(
                id = R.string.lecture_view_attachments_file_info_version_date,
                version,
                formattedUploadDate
            )

        version != null ->
            stringResource(id = R.string.lecture_view_attachments_file_info_version, version)

        formattedUploadDate != null -> stringResource(
            id = R.string.lecture_view_attachments_file_info_date,
            formattedUploadDate
        )

        else -> null
    }

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = attachment.name.orEmpty()) },
        supportingContent = supportingContent?.let { { Text(text = it) } },
        leadingContent = {
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = imageVector,
                contentDescription = null
            )
        }
    )
}