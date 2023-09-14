package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitUnknown
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitAttachmentUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitExerciseUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitHeader
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitOnlineUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitTextUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitVideoUi

internal const val TEST_TAG_OVERVIEW_LIST = "overview_list"

internal fun getLectureUnitTestTag(lectureUnitId: Long) = "LectureUnit$lectureUnitId"

@Composable
internal fun OverviewTab(
    modifier: Modifier,
    description: String?,
    lectureUnits: List<LectureUnitData>,
    onViewExercise: (exerciseId: Long) -> Unit,
    onMarkAsCompleted: (lectureUnitId: Long, isCompleted: Boolean) -> Unit,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    exerciseActions: BoundExerciseActions,
    state: LazyListState
) {
    LazyColumn(
        modifier = modifier.testTag(TEST_TAG_OVERVIEW_LIST),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = state
    ) {
        if (description != null) {
            item {
                DescriptionSection(
                    modifier = Modifier.fillMaxWidth(),
                    description = description
                )
            }
        }

        if (lectureUnits.isNotEmpty()) {
            lectureUnitSection(
                modifier = Modifier.fillMaxWidth(),
                lectureUnits = lectureUnits,
                onViewExercise = onViewExercise,
                onMarkAsCompleted = onMarkAsCompleted,
                onRequestViewLink = onRequestViewLink,
                onRequestOpenAttachment = onRequestOpenAttachment,
                exerciseActions = exerciseActions
            )
        }
    }
}

@Composable
private fun DescriptionSection(modifier: Modifier, description: String) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_description),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        MarkdownText(
            markdown = description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun LazyListScope.lectureUnitSection(
    modifier: Modifier,
    lectureUnits: List<LectureUnitData>,
    onViewExercise: (exerciseId: Long) -> Unit,
    onMarkAsCompleted: (lectureUnitId: Long, isCompleted: Boolean) -> Unit,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    exerciseActions: BoundExerciseActions
) {
    item {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.lecture_view_overview_section_lecture_units),
            style = MaterialTheme.typography.headlineMedium
        )
    }

    lectureUnits.forEachIndexed { index, lectureUnitWithData ->
        item(lectureUnitWithData.lectureUnit.id) {
            LectureUnitListItem(
                modifier = modifier.testTag(getLectureUnitTestTag(lectureUnitWithData.lectureUnit.id)),
                lectureUnit = lectureUnitWithData.lectureUnit,
                isUploadingMarkedAsCompleted = lectureUnitWithData.isUploadingChanges,
                onViewExercise = onViewExercise,
                onMarkAsCompleted = { isCompleted ->
                    onMarkAsCompleted(lectureUnitWithData.lectureUnit.id, isCompleted)
                },
                onRequestViewLink = onRequestViewLink,
                onRequestOpenAttachment = onRequestOpenAttachment,
                exerciseActions = exerciseActions
            )
        }

        if (index < lectureUnits.size - 1) {
            item {
                Divider()
            }
        }
    }
}

@Composable
private fun LectureUnitListItem(
    modifier: Modifier,
    lectureUnit: LectureUnit,
    isUploadingMarkedAsCompleted: Boolean,
    onMarkAsCompleted: (isCompleted: Boolean) -> Unit,
    onViewExercise: (exerciseId: Long) -> Unit,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    exerciseActions: BoundExerciseActions
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val childModifier = Modifier.fillMaxWidth()

        LectureUnitHeader(
            modifier = childModifier,
            lectureUnit = lectureUnit,
            onMarkAsCompleted = onMarkAsCompleted,
            isUploadingMarkedAsCompleted = isUploadingMarkedAsCompleted
        )

        when (lectureUnit) {
            is LectureUnitAttachment -> {
                LectureUnitAttachmentUi(
                    modifier = childModifier,
                    lectureUnit = lectureUnit,
                    onClickOpenLink = {
                        onRequestOpenAttachment(
                            lectureUnit.attachment ?: return@LectureUnitAttachmentUi
                        )
                    }
                )
            }

            is LectureUnitExercise -> {
                LectureUnitExerciseUi(
                    modifier = childModifier,
                    lectureUnit = lectureUnit,
                    onClickExercise = onViewExercise,
                    exerciseActions = exerciseActions
                )
            }

            is LectureUnitOnline -> {
                LectureUnitOnlineUi(
                    modifier = childModifier,
                    lectureUnit = lectureUnit,
                    onClickOpenLink = {
                        onRequestViewLink(lectureUnit.source.orEmpty())
                    }
                )
            }

            is LectureUnitText -> {
                LectureUnitTextUi(
                    modifier = childModifier,
                    lectureUnit = lectureUnit
                )
            }

            is LectureUnitUnknown -> {}
            is LectureUnitVideo -> {
                LectureUnitVideoUi(
                    modifier = childModifier,
                    lectureUnit = lectureUnit,
                    onClickOpenLink = {
                        onRequestViewLink(lectureUnit.source.orEmpty())
                    }
                )
            }
        }
    }
}