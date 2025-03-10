package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItem
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.util.CourseUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.R

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
fun CourseList(
    modifier: Modifier,
    courses: List<CourseWithScore>,
    courseListState: LazyGridState,
    recentCourses: List<CourseWithScore>,
    onClickOnCourse: (Course) -> Unit
) {
    val windowSizeClass = getWindowSizeClass()
    val columnCount = CourseUtil.computeCourseColumnCount(windowSizeClass)

    val courseItemModifier = Modifier
        .height(Spacings.CourseItem.height)
        .fillMaxWidth()

    LazyVerticalGrid(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        columns = GridCells.Fixed(columnCount),
        state = courseListState,
        verticalArrangement = Arrangement.spacedBy(Spacings.CourseItem.gridSpacing),
        horizontalArrangement = Arrangement.spacedBy(Spacings.CourseItem.gridSpacing),
        contentPadding = Spacings.calculateEndOfPagePaddingValues()
    ) {
        if (recentCourses.isNotEmpty()) {
            item(span = { GridItemSpan(columnCount) }) {
                SectionHeader(
                    modifier = Modifier.animateItem(),
                    text = stringResource(R.string.course_overview_recently_accessed_section)
                )
            }
        }

        items(recentCourses, key = { it.course.id ?: 0L }) { course ->
            CourseItem(
                modifier = courseItemModifier
                    .animateItem()
                    .testTag(testTagForCourse(course.course.id!!)),
                courseWithScore = course,
                onClick = { onClickOnCourse(course.course) }
            )
        }

        if (courses.isNotEmpty() && recentCourses.isNotEmpty()) {
            item(span = { GridItemSpan(columnCount) }) {
                SectionHeader(
                    modifier = Modifier.animateItem(),
                    text = stringResource(R.string.course_overview_all_courses_section)
                )
            }
        }

        items(courses, key = { it.course.id ?: 0L }) { course ->
            CourseItem(
                modifier = courseItemModifier
                    .animateItem()
                    .testTag(testTagForCourse(course.course.id!!)),
                courseWithScore = course,
                onClick = { onClickOnCourse(course.course) }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier,
    text: String
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
}