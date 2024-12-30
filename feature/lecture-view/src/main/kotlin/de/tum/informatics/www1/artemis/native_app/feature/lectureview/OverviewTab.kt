package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch

internal const val TEST_TAG_OVERVIEW_LIST = "overview_list"

internal fun getLectureUnitTestTag(lectureUnitId: Long) = "LectureUnit$lectureUnitId"

@OptIn(ExperimentalMaterial3Api::class)
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
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedLectureUnit: LectureUnit? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()

    // Only render the bottom sheet when selectedLectureUnit is not null
    if (selectedLectureUnit != null) {
        ModalBottomSheet(
            modifier = Modifier.padding(
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            ),
            sheetState = bottomSheetState,
            onDismissRequest = { selectedLectureUnit = null }
        ) {
            LectureUnitBottomSheetContent(
                modifier = modifier,
                lectureUnit = selectedLectureUnit!!,
                onViewExercise = onViewExercise,
                onRequestViewLink = onRequestViewLink,
                onRequestOpenAttachment = onRequestOpenAttachment,
                exerciseActions = exerciseActions
            )
        }
    }

    LazyColumn(
        modifier = modifier.testTag(TEST_TAG_OVERVIEW_LIST),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = state,
        contentPadding = PaddingValues(
            bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
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
                onMarkAsCompleted = onMarkAsCompleted,
                onHeaderClick = { lectureUnit ->
                    selectedLectureUnit = lectureUnit
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            )
        }
    }
}

@Composable
private fun DescriptionSection(modifier: Modifier, description: String) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_description),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            )
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
    onMarkAsCompleted: (lectureUnitId: Long, isCompleted: Boolean) -> Unit,
    onHeaderClick: (LectureUnit) -> Unit
) {
    item {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.lecture_view_overview_section_lecture_units),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    lectureUnits.forEachIndexed { index, lectureUnitWithData ->
        item(lectureUnitWithData.lectureUnit.id) {
            LectureUnitHeader(
                modifier = modifier.testTag(getLectureUnitTestTag(lectureUnitWithData.lectureUnit.id)),
                lectureUnit = lectureUnitWithData.lectureUnit,
                onMarkAsCompleted = { isCompleted ->
                    onMarkAsCompleted(lectureUnitWithData.lectureUnit.id, isCompleted)
                },
                isUploadingMarkedAsCompleted = lectureUnitWithData.isUploadingChanges,
                onHeaderClick = { onHeaderClick(lectureUnitWithData.lectureUnit) }
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
private fun LectureUnitBottomSheetContent(
    modifier: Modifier,
    lectureUnit: LectureUnit,
    onViewExercise: (exerciseId: Long) -> Unit,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    exerciseActions: BoundExerciseActions
) {
    val childModifier = Modifier.fillMaxWidth()
    Column(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
