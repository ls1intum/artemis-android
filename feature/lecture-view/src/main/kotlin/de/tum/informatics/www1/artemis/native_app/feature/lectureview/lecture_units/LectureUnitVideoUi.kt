package de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.R

@Composable
internal fun LectureUnitVideoUi(
    modifier: Modifier,
    lectureUnit: LectureUnitAttachmentVideo,
    onClickOpenLink: () -> Unit,
    onClickOpenAttachment: (Attachment) -> Unit,
) {
    LectureUnitWithLinkUi(
        modifier = modifier,
        name = lectureUnit.name.orEmpty(),
        text = lectureUnit.description,
        onClickOpenLink = onClickOpenLink,
        trailingContent = {
            if (lectureUnit.hasAttachment) {
                AttachmentButton(onClick = { lectureUnit.attachment?.let(onClickOpenAttachment) })
            }
        }
    )
}

@Composable
private fun ColumnScope.AttachmentButton(
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        onClick = onClick
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null
            )

            Text(
                text = stringResource(id = R.string.lecture_view_open_attachment_button)
            )
        }
    }
}