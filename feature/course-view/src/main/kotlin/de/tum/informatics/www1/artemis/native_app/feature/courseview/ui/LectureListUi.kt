package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrame

internal const val TEST_TAG_LECTURE_LIST = "lecture list"

@Composable
internal fun LectureListUi(
    modifier: Modifier,
    lectures: List<TimeFrame<Lecture>>,
    collapsingContentState: CollapsingContentState,
    query: String,
    onClickLecture: (Lecture) -> Unit
) {
    if (lectures.isEmpty()) {
        if (query.isNotBlank()) {
            NoSearchResults(
                modifier = modifier,
                title = stringResource(id = R.string.course_ui_lectures_no_search_results_title),
                details = stringResource(id = R.string.course_ui_lectures_no_search_results_body, query)
            )
            return
        }

        EmptyListHint(
            modifier = modifier,
            hint = stringResource(id = R.string.course_ui_lectures_no_search_results_title),
            imageVector = Icons.Default.School
        )
        return
    }

    TimeFrameItemsLazyColumn(
        modifier = modifier
            .nestedScroll(collapsingContentState.nestedScrollConnection)
            .testTag(TEST_TAG_LECTURE_LIST),
        timeFrameGroup = lectures,
        query = query,
        getItemId = { id ?: 0L }
    ) { m, lecture ->
        LectureListItem(
            modifier = m
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            lecture = lecture,
            onClick = { onClickLecture(lecture) }
        )
    }
}

@Composable
private fun LectureListItem(modifier: Modifier, lecture: Lecture, onClick: () -> Unit) {
    val startTime = lecture.startDate
    val startTimeText = if (startTime != null) {
        stringResource(
            id = R.string.course_ui_lecture_item_start_date_set,
            getRelativeTime(to = startTime)
        )
    } else stringResource(id = R.string.course_ui_lecture_item_start_date_not_set)

    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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