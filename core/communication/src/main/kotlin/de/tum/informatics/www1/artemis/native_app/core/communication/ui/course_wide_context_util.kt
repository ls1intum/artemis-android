package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Support
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext

fun getIconForCourseWideContext(courseWideContext: CourseWideContext) = when (courseWideContext) {
    CourseWideContext.TECH_SUPPORT -> Icons.Default.Support
    CourseWideContext.ORGANIZATION -> Icons.Default.CalendarMonth
    CourseWideContext.RANDOM -> Icons.Default.QuestionMark
    CourseWideContext.ANNOUNCEMENT -> Icons.Default.Campaign
}

@Composable
fun getHumanReadableTextForCourseWideContext(courseWideContext: CourseWideContext) = stringResource(
    id = when (courseWideContext) {
        CourseWideContext.TECH_SUPPORT -> R.string.course_wide_context_tech_support
        CourseWideContext.ORGANIZATION -> R.string.course_wide_context_organization
        CourseWideContext.RANDOM -> R.string.course_wide_context_random
        CourseWideContext.ANNOUNCEMENT -> R.string.course_wide_context_announcement
    }
)