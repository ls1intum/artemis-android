package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Support
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext

fun getIconForCourseWideContext(courseWideContext: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext) = when (courseWideContext) {
    de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.TECH_SUPPORT -> Icons.Default.Support
    de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.ORGANIZATION -> Icons.Default.CalendarMonth
    de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.RANDOM -> Icons.Default.QuestionMark
    de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.ANNOUNCEMENT -> Icons.Default.Campaign
}

@Composable
fun getHumanReadableTextForCourseWideContext(courseWideContext: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext) = stringResource(
    id = when (courseWideContext) {
        de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.TECH_SUPPORT -> R.string.course_wide_context_tech_support
        de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.ORGANIZATION -> R.string.course_wide_context_organization
        de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.RANDOM -> R.string.course_wide_context_random
        de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext.ANNOUNCEMENT -> R.string.course_wide_context_announcement
    }
)