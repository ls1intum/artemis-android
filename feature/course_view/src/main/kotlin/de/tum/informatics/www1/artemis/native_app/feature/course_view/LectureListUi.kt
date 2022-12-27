package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime

@Composable
internal fun LectureListUi(
    modifier: Modifier,
    lectures: List<GroupedByWeek<Lecture>>,
    onClickLecture: (Lecture) -> Unit
) {
    WeeklyItemsLazyColumn(
        modifier = modifier,
        weeklyItemGroups = lectures,
        getItemId = Lecture::id
    ) { lecture ->
        LectureListItem(
            modifier = Modifier.fillMaxWidth(),
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

    Box(modifier = modifier.clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = lecture.title,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = startTimeText,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}