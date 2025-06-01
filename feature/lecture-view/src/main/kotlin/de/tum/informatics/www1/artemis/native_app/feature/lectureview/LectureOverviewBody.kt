package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrameItemsLazyColumn
import de.tum.informatics.www1.artemis.native_app.core.ui.common.selectionBorder
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime

const val TEST_TAG_LECTURE_LIST = "lecture list"

@Composable
fun LectureOverviewBody(
    modifier: Modifier,
    lectures: List<TimeFrame<Lecture>>,
    collapsingContentState: CollapsingContentState,
    query: String,
    onSelectLecture: (Long) -> Unit,
    selectedLectureId: Long?
) {
    LectureListUi(
        modifier = modifier,
        lectures = lectures,
        collapsingContentState = collapsingContentState,
        query = query,
        onClickLecture = { lec -> onSelectLecture(lec.id ?: 0L) },
        selectedLectureId = selectedLectureId
    )
}

@Composable
internal fun LectureListUi(
    modifier: Modifier,
    lectures: List<TimeFrame<Lecture>>,
    collapsingContentState: CollapsingContentState,
    query: String,
    onClickLecture: (Lecture) -> Unit,
    selectedLectureId: Long?
) {
    if (lectures.isEmpty()) {
        if (query.isNotBlank()) {
            NoSearchResults(
                modifier = modifier,
                title = stringResource(id = R.string.lecture_list_lectures_no_search_results_title),
                details = stringResource(
                    id = R.string.lecture_list_lectures_no_search_results_body,
                    query
                )
            )
        } else {
            EmptyListHint(
                modifier = modifier,
                hint = stringResource(id = R.string.lecture_list_lectures_no_search_results_title),
                imageVector = Icons.Default.School
            )
        }
        return
    }

    TimeFrameItemsLazyColumn(
        modifier = modifier
            .nestedScroll(collapsingContentState.nestedScrollConnection)
            .testTag(TEST_TAG_LECTURE_LIST),
        timeFrameGroup = lectures,
        query = query,
        getItemId = { id ?: 0L }
    ) { itemMod, lecture ->
        LectureListItem(
            modifier = itemMod
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            lecture = lecture,
            onClick = { onClickLecture(lecture) },
            selected = selectedLectureId == lecture.id
        )
    }
}

@Composable
private fun LectureListItem(
    modifier: Modifier,
    lecture: Lecture,
    onClick: () -> Unit,
    selected: Boolean
) {
    val startTime = lecture.startDate
    val startTimeText = if (startTime != null) {
        stringResource(
            id = R.string.lecture_list_lecture_item_start_date_set,
            getRelativeTime(to = startTime)
        )
    } else stringResource(id = R.string.lecture_list_lecture_item_start_date_not_set)

    Card(
        modifier = modifier.selectionBorder(selected),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,

            ) {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 16.dp)
                        .fillMaxSize(),
                    painter = painterResource(id = R.drawable.chalkboard_teacher),
                    contentDescription = null
                )

                Text(
                    text = lecture.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                modifier = Modifier,
                text = startTimeText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}