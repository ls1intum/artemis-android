package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider

private val headerHeight = 80.dp

@Composable
fun CourseItemGrid(
    modifier: Modifier,
    courses: List<CourseWithScore>,
    courseItem: @Composable LazyGridItemScope.(CourseWithScore, Modifier, isCompact: Boolean) -> Unit
) {
    val windowSizeClass = getWindowSizeClass()
    val columnCount = computeCourseColumnCount(windowSizeClass)

    val isCompact = windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Compact
    val courseItemModifier = Modifier.computeCourseItemModifier(isCompact)

    LazyVerticalGrid(
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        columns = GridCells.Fixed(columnCount),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = Spacings.calculateEndOfPagePadding()
    ) {
        items(courses, key = { it.course.id ?: 0L }) { course ->
            courseItem(course, courseItemModifier, isCompact)
        }
    }
}

fun Modifier.computeCourseItemModifier(isCompact: Boolean): Modifier {
    return if (isCompact) {
        fillMaxWidth()
    } else {
        fillMaxWidth()
            .aspectRatio(1f)
    }
}

/**
 * Displays the cource icon on left, with the title and the description in a column on the right to it.
 */
@Composable
fun CompactCourseItemHeader(
    modifier: Modifier,
    course: Course,
    compactCourseHeaderViewMode: CompactCourseHeaderViewMode,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val painter = getCourseIconPainter(course)

    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    modifier = Modifier
                        .size(headerHeight),
                    painter = painter,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    AutoResizeText(
                        text = course.title,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSizeRange = FontSizeRange(min = 14.sp, max = 22.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    when (compactCourseHeaderViewMode) {
                        CompactCourseHeaderViewMode.DESCRIPTION -> {
                            Text(
                                text = course.description,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp,
                                maxLines = 3
                            )
                        }

                        CompactCourseHeaderViewMode.EXERCISE_AND_LECTURE_COUNT -> {
                            CourseExerciseAndLectureCount(
                                modifier = Modifier.fillMaxWidth(),
                                exerciseCount = course.exercises.size,
                                lectureCount = course.lectures.size,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun getCourseIconPainter(
    course: Course,
): Painter {
    return if (course.courseIconPath != null) {
        LocalArtemisImageProvider.current.rememberArtemisAsyncImagePainter(
            imagePath = course.courseIconPath.orEmpty()
        )
    } else rememberVectorPainter(image = Icons.Default.QuestionMark)
}

@Composable
fun ExpandedCourseItemHeader(
    modifier: Modifier,
    course: Course,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
    rightHeaderContent: @Composable BoxScope.() -> Unit
) {
    val courseIconPainter = getCourseIconPainter(course)

    val courseColor: Color? = remember {
        try {
            course.color?.toColorInt()?.let { Color(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    Card(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .let {
                    if (courseColor != null) {
                        it.background(courseColor)
                    } else it
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val courseIconModifier = Modifier
                .weight(2f)

            if (course.courseIconPath != null) {
                Box(modifier = courseIconModifier) {
                    Image(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape),
                        painter = courseIconPainter,
                        contentDescription = null
                    )
                }
            } else {
                Box(modifier = courseIconModifier)
            }

            AutoResizeText(
                modifier = Modifier.weight(6f),
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSizeRange = FontSizeRange(min = 14.sp, max = 18.sp)
            )

            Box(
                modifier = courseIconModifier,
                content = rightHeaderContent
            )
        }

        content()
    }
}

/**
 * Text about the amount of exercises and lectures the course has.
 */
@Composable
fun CourseExerciseAndLectureCount(
    modifier: Modifier,
    textStyle: TextStyle,
    exerciseCount: Int,
    lectureCount: Int,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = alignment
    ) {
        ProvideTextStyle(value = textStyle) {
            Text(
                text = stringResource(
                    id = R.string.course_header_exercise_count,
                    exerciseCount
                )
            )

            Text(
                text = stringResource(
                    id = R.string.course_header_lecture_count,
                    lectureCount
                )
            )
        }
    }
}

fun computeCourseColumnCount(windowSizeClass: WindowSizeClass): Int = when {
    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded -> 3
    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium -> 2
    else -> 1
}

enum class CompactCourseHeaderViewMode {
    DESCRIPTION,
    EXERCISE_AND_LECTURE_COUNT
}