package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItem
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItemGrid

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
fun CourseList(
    modifier: Modifier,
    courses: List<CourseWithScore>,
    onClickOnCourse: (Course) -> Unit
) {
    CourseItemGrid(
        modifier = modifier,
        courses = courses,
    ) { dashboardCourse, courseItemModifier, isCompact ->
        CourseItem(
            modifier = courseItemModifier.testTag(testTagForCourse(dashboardCourse.course.id!!)),
            courseWithScore = dashboardCourse,
            onClick = { onClickOnCourse(dashboardCourse.course) },
            isCompact = isCompact
        )
    }
}