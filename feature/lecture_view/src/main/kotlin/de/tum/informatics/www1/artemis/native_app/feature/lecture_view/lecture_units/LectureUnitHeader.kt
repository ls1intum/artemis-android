package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.R

/**
 * @param isUploadingMarkedAsCompleted if we are currently uploading this a change the user has requested
 */
@Composable
internal fun LectureUnitHeader(
    modifier: Modifier,
    lectureUnit: LectureUnit,
    isUploadingMarkedAsCompleted: Boolean,
    onMarkAsCompleted: (isCompleted: Boolean) -> Unit
) {
    val (icon, text) = when (lectureUnit) {
        is LectureUnitAttachment -> Icons.Default.Description to R.string.lecture_view_lecture_unit_type_attachment
        is LectureUnitExercise -> Icons.Default.Task to R.string.lecture_view_lecture_unit_type_exercise
        is LectureUnitOnline -> Icons.Default.Link to R.string.lecture_view_lecture_unit_type_online
        is LectureUnitText -> Icons.Default.Assignment to R.string.lecture_view_lecture_unit_type_text
        is LectureUnitUnknown -> Icons.Default.QuestionMark to R.string.lecture_view_lecture_unit_type_unknown
        is LectureUnitVideo -> Icons.Default.Videocam to R.string.lecture_view_lecture_unit_type_video
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = icon,
            contentDescription = null
        )

        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (lectureUnit !is LectureUnitExercise) {
            Crossfade(targetState = isUploadingMarkedAsCompleted) { isUploadingState ->
                if (isUploadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Checkbox(checked = lectureUnit.completed, onCheckedChange = onMarkAsCompleted)
                }
            }
        }
    }
}