package de.tum.informatics.www1.artemis.native_app.core.ui.common.course.util

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider

object CourseUtil {

    fun computeCourseColumnCount(windowSizeClass: WindowSizeClass): Int = when {
        windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded -> 2
        windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium -> 2
        else -> 1
    }

    @Composable
    fun getCourseIconPainter(
        course: Course,
    ): Painter? {
        return if (course.courseIconPath != null) {
            LocalArtemisImageProvider.current.rememberArtemisAsyncImagePainter(
                imagePath = course.courseIconPath.orEmpty()
            )
        } else null
    }
}