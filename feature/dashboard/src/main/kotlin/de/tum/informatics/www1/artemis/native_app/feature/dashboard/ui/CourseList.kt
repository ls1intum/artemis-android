package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseHeaderViewMode
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseExerciseAndLectureCount
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItemGrid
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.ExpandedCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.CoursePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.R
import java.text.DecimalFormat

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
fun CourseList(
    modifier: Modifier,
    courses: List<CourseWithScore>,
    onRefresh: () -> Unit,
    onClickOnCourse: (Course) -> Unit
) {
    CourseItemGrid(
        modifier = modifier,
        courses = courses,
        onRefresh = onRefresh,
    ) { dashboardCourse, courseItemModifier, isCompact ->
        CourseItem(
            modifier = courseItemModifier.testTag(testTagForCourse(dashboardCourse.course.id!!)),
            courseWithScore = dashboardCourse,
            onClick = { onClickOnCourse(dashboardCourse.course) },
            isCompact = isCompact
        )
    }
}

/**
 * Displays course icon, title and description in a Material Design Card.
 */
@Composable
fun CourseItem(
    modifier: Modifier,
    isCompact: Boolean,
    courseWithScore: CourseWithScore,
    onClick: () -> Unit
) {
    val currentPoints = courseWithScore.totalScores.studentScores.absoluteScore
    val maxPoints = courseWithScore.totalScores.maxPoints

    val currentPointsFormatted = remember(currentPoints) {
        CoursePointsDecimalFormat.format(currentPoints)
    }
    val maxPointsFormatted = remember(maxPoints) {
        CoursePointsDecimalFormat.format(maxPoints)
    }

    val progress = if (maxPoints == 0f) 0f else currentPoints / maxPoints

    val progressPercentFormatted = remember(progress) {
        DecimalFormat.getPercentInstance().format(progress)
    }

    if (isCompact) {
        CompactCourseItemHeader(
            modifier = modifier,
            course = courseWithScore.course,
            onClick = onClick,
            compactCourseHeaderViewMode = CompactCourseHeaderViewMode.EXERCISE_AND_LECTURE_COUNT,
            content = {
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f),
                        trackColor = MaterialTheme.colorScheme.onPrimary,
                    )

                    CourseProgressText(
                        modifier = Modifier,
                        currentPointsFormatted = currentPointsFormatted,
                        maxPointsFormatted = maxPointsFormatted,
                        progressPercentFormatted = progressPercentFormatted
                    )
                }
            }
        )
    } else {
        ExpandedCourseItemHeader(
            modifier = modifier,
            course = courseWithScore.course,
            onClick = onClick,
            content = {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularCourseProgress(
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .align(Alignment.Center),
                        progress = progress,
                        currentPointsFormatted = currentPointsFormatted,
                        maxPointsFormatted = maxPointsFormatted,
                        progressPercentFormatted = progressPercentFormatted
                    )
                }

                CourseExerciseAndLectureCount(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp),
                    exerciseCount = courseWithScore.course.exercises.size,
                    lectureCount = courseWithScore.course.lectures.size,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    alignment = Alignment.CenterHorizontally
                )
            },
            rightHeaderContent = { }
        )
    }
}


@Composable
private fun CircularCourseProgress(
    modifier: Modifier,
    progress: Float,
    currentPointsFormatted: String,
    maxPointsFormatted: String,
    progressPercentFormatted: String
) {
    BoxWithConstraints(modifier = modifier) {
        val progressBarWidthDp = min(24.dp, maxWidth * 0.1f)
        val progressBarWidth = with(LocalDensity.current) { progressBarWidthDp.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(progressBarWidthDp)
        ) {
            drawArc(
                color = Color.Green,
                startAngle = 180f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )

            drawArc(
                color = Color.Red,
                startAngle = 180f + 360f * progress,
                sweepAngle = 360f * (1f - progress),
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )
        }

        val (percentFontSize, ptsFontSize) = with(LocalDensity.current) {
            val availableSpace = maxHeight - progressBarWidthDp * 2
            (availableSpace * 0.2f).toSp() to (availableSpace * 0.1f).toSp()
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    id = R.string.course_overview_course_progress_percentage,
                    progressPercentFormatted
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontSize = percentFontSize,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = stringResource(
                    id = R.string.course_overview_course_progress_pts,
                    currentPointsFormatted,
                    maxPointsFormatted
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontSize = ptsFontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CourseProgressText(
    modifier: Modifier,
    currentPointsFormatted: String,
    maxPointsFormatted: String,
    progressPercentFormatted: String
) {
    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.course_overview_course_progress,
            currentPointsFormatted,
            maxPointsFormatted,
            progressPercentFormatted
        ),
        fontSize = 14.sp
    )
}