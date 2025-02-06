package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItem
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.util.CourseUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
fun CourseList(
    modifier: Modifier,
    courses: List<CourseWithScore>,
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
        verticalArrangement = Arrangement.spacedBy(Spacings.CourseItem.gridSpacing),
        horizontalArrangement = Arrangement.spacedBy(Spacings.CourseItem.gridSpacing),
        contentPadding = Spacings.calculateEndOfPagePaddingValues()
    ) {
        items(courses, key = { it.course.id ?: 0L }) { course ->
            CourseItem(
                modifier = courseItemModifier.testTag(testTagForCourse(course.course.id!!)),
                courseWithScore = course,
                onClick = { onClickOnCourse(course.course) }
            )
        }
    }
}