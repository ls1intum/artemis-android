package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitUnknown
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.date.DateFormats
import de.tum.informatics.www1.artemis.native_app.core.ui.date.format
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitHeader
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitOnlineUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitTextUi
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitVideoUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common.getChannelIconImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

internal const val TEST_TAG_OVERVIEW_LIST = "overview_list"

internal fun getLectureUnitTestTag(lectureUnitId: Long) = "LectureUnit$lectureUnitId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LectureOverviewTab(
    modifier: Modifier,
    lecture: Lecture,
    lectureChannel: DataState<ChannelChat>,
    lectureUnits: List<LectureUnitData>,
    onViewExercise: (exerciseId: Long) -> Unit,
    onMarkAsCompleted: (lectureUnitId: Long, isCompleted: Boolean) -> Unit,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    state: LazyListState
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedLectureUnit: LectureUnit? by remember { mutableStateOf(null) }
    val channel = lectureChannel.bind { it }.orNull()
    val startDate = lecture.startDate
    val endDate = lecture.endDate
    val description = lecture.description
    val coroutineScope = rememberCoroutineScope()

    // Only render the bottom sheet when selectedLectureUnit is not null
    if (selectedLectureUnit != null) {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            contentWindowInsets = { WindowInsets.statusBars },
            sheetState = bottomSheetState,
            onDismissRequest = { selectedLectureUnit = null }
        ) {
            LectureUnitBottomSheetContent(
                modifier = modifier,
                lectureUnit = selectedLectureUnit ?: return@ModalBottomSheet,
                onRequestViewLink = onRequestViewLink
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .testTag(TEST_TAG_OVERVIEW_LIST),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = state,
        contentPadding = Spacings.calculateContentPaddingValues()
    ) {
        startDate?.let {
            dateSection(
                modifier = Modifier.fillMaxWidth(),
                startDate = it,
                endDate = endDate
            )
        }

        description?.let {
            descriptionSection(
                modifier = Modifier.fillMaxWidth(),
                description = it
            )
        }

        if (lectureUnits.isNotEmpty()) {
            lectureUnitSection(
                modifier = Modifier.fillMaxWidth(),
                lectureUnits = lectureUnits,
                onViewExercise = onViewExercise,
                onMarkAsCompleted = onMarkAsCompleted,
                onRequestOpenAttachment = onRequestOpenAttachment,
                onHeaderClick = { lectureUnit ->
                    selectedLectureUnit = lectureUnit
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            )
        }

        channel?.let {
            channelSection(
                modifier = Modifier.fillMaxWidth(),
                channel = channel,
                lecture = lecture
            )
        }
    }
}

private fun LazyListScope.dateSection(
    modifier: Modifier,
    startDate: Instant,
    endDate: Instant?
) {
    val dateRange = listOfNotNull(
        startDate,
        endDate
    ).joinToString(" - ") { it.format(DateFormats.DefaultDateAndTime.format) }

    stickyHeader {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_date),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    item {
        Text(
            text = dateRange,
            modifier = modifier.animateItem(),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun LazyListScope.descriptionSection(
    modifier: Modifier,
    description: String
) {
    stickyHeader {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_description),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    item {
        MarkdownText(
            modifier = modifier.animateItem(),
            markdown = description,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun LazyListScope.channelSection(
    modifier: Modifier,
    lecture: Lecture,
    channel: ChannelChat
) {
    stickyHeader {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_communication),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    item {
        val localLinkOpener = LocalLinkOpener.current

        Card(
            modifier = modifier
                .animateItem()
                .fillMaxWidth(),
            onClick = {
                val courseId = lecture.course?.id
                courseId?.let {
                    localLinkOpener.openLink(
                        CommunicationDeeplinks.ToConversation.inAppLink(
                            it,
                            channel.id
                        )
                    )
                }
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        modifier = Modifier.height(24.dp),
                        imageVector = getChannelIconImageVector(channel),
                        contentDescription = null
                    )

                    Text(
                        text = channel.humanReadableName.removePrefix("lecture-"),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Icon(
                    modifier = Modifier.height(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }
    }
}

private fun LazyListScope.lectureUnitSection(
    modifier: Modifier,
    lectureUnits: List<LectureUnitData>,
    onViewExercise: (exerciseId: Long) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    onMarkAsCompleted: (lectureUnitId: Long, isCompleted: Boolean) -> Unit,
    onHeaderClick: (LectureUnit) -> Unit
) {
    stickyHeader {
        Text(
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            text = stringResource(id = R.string.lecture_view_overview_section_lecture_units),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    lectureUnits.forEachIndexed { _, lectureUnitWithData ->
        item(lectureUnitWithData.lectureUnit.id) {
            LectureUnitHeader(
                modifier = modifier
                    .testTag(getLectureUnitTestTag(lectureUnitWithData.lectureUnit.id))
                    .animateItem(),
                lectureUnit = lectureUnitWithData.lectureUnit,
                onClickExercise = onViewExercise,
                onMarkAsCompleted = { isCompleted ->
                    onMarkAsCompleted(lectureUnitWithData.lectureUnit.id, isCompleted)
                },
                isUploadingMarkedAsCompleted = lectureUnitWithData.isUploadingChanges,
                onRequestOpenAttachment = onRequestOpenAttachment,
                onHeaderClick = { onHeaderClick(lectureUnitWithData.lectureUnit) }
            )
        }
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LectureUnitBottomSheetContent(
    modifier: Modifier,
    lectureUnit: LectureUnit,
    onRequestViewLink: (String) -> Unit
) {
    val childModifier = Modifier.fillMaxWidth()
    Column(
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when (lectureUnit) {
            is LectureUnitAttachment -> {}

            is LectureUnitExercise -> {}

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
