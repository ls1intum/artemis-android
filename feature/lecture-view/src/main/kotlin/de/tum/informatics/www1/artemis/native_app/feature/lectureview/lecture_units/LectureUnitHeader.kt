package de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitUnknown
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.RoundGreenCheckbox
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.R

internal const val TEST_TAG_CHECKBOX_LECTURE_UNIT_COMPLETED = "Checkbox Lecture Unit Completed"

/**
 * @param isUploadingMarkedAsCompleted if we are currently uploading this a change the user has requested
 */
@Composable
internal fun LectureUnitHeader(
    modifier: Modifier,
    lectureUnit: LectureUnit,
    onClickExercise: (exerciseId: Long) -> Unit,
    isUploadingMarkedAsCompleted: Boolean,
    onMarkAsCompleted: (isCompleted: Boolean) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    onHeaderClick: () -> Unit
) {
    val (icon, text) = when (lectureUnit) {
        is LectureUnitAttachmentVideo -> {
            val icon = if (lectureUnit.hasVideo) {
                Icons.Default.Videocam
            } else {
                Icons.Default.Description
            }
            val text = if (lectureUnit.hasVideo) {
                R.string.lecture_view_lecture_unit_type_video
            } else {
                R.string.lecture_view_lecture_unit_type_attachment
            }
            icon to text
        }
        is LectureUnitExercise -> {
            val exercise = lectureUnit.exercise ?: return
            val exerciseId = exercise.id ?: return
            ExerciseListItem(
                modifier = modifier,
                exercise = exercise,
                onClickExercise = { onClickExercise(exerciseId) }
            )
            return
        }
        is LectureUnitOnline -> Icons.Default.Link to R.string.lecture_view_lecture_unit_type_online
        is LectureUnitText -> Icons.AutoMirrored.Default.Assignment to R.string.lecture_view_lecture_unit_type_text
        is LectureUnitUnknown -> Icons.Default.QuestionMark to R.string.lecture_view_lecture_unit_type_unknown
    }

    Card(
        modifier = modifier,
        onClick = {
            onMarkAsCompleted(true)
            if (lectureUnit is LectureUnitAttachmentVideo) {
                onRequestOpenAttachment(lectureUnit.attachment ?: return@Card)
                return@Card
            }
            onHeaderClick()
        }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                contentDescription = null
            )

            Text(
                text = lectureUnit.name ?: stringResource(id = text),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            UploadingCheckbox(
                isUploading = isUploadingMarkedAsCompleted,
                checked = lectureUnit.completed,
                onCheckedChange = { isChecked ->
                    onMarkAsCompleted(isChecked)
                }
            )
        }
    }
}

@Composable
private fun UploadingCheckbox(
    modifier: Modifier = Modifier,
    isUploading: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        val alphaProgress by animateFloatAsState(
            targetValue = if (isUploading) 1f else 0f
        )
        val alphaCheckbox by animateFloatAsState(
            targetValue = if (isUploading) 0f else 1f
        )

        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaProgress)
        )

        RoundGreenCheckbox(
            modifier = Modifier
                .testTag(TEST_TAG_CHECKBOX_LECTURE_UNIT_COMPLETED)
                .alpha(alphaCheckbox),
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}