package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime

@Composable
internal fun AttachmentsSection(
    modifier: Modifier,
    attachments: List<Attachment>,
    onClickFileAttachment: (Attachment) -> Unit,
    onClickOpenLinkAttachment: (Attachment) -> Unit
) {
    if (attachments.isNotEmpty()) {
        LazyColumn(
            modifier = modifier,
            contentPadding = Spacings.calculateContentPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                if (attachment != attachments.last()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    } else {
        EmptyListHint(
            modifier = Modifier,
            hint = stringResource(id = R.string.lecture_view_attachments_empty),
            painter = painterResource(de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R.drawable.attachment)
        )
    }
}

@Composable
private fun AttachmentItem(modifier: Modifier, attachment: Attachment, onClick: () -> Unit) {
    val contentDescription = when (attachment.attachmentType) {
        Attachment.AttachmentType.FILE -> stringResource(id = R.string.lecture_view_attachments_pdf)
        Attachment.AttachmentType.URL -> stringResource(id = R.string.lecture_view_attachments_link)
    }

    val version = attachment.version
    val uploadDate = attachment.uploadDate
    val formattedUploadDate = uploadDate?.let { getRelativeTime(to = it, showDateAndTime = true) }

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

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = attachment.name.orEmpty(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = contentDescription,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        supportingContent?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}