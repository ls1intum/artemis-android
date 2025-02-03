package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.latestParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.upcomingExercises
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.CoursePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.CourseColors
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import java.text.DecimalFormat

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
        DecimalFormat("0.##%").format(progress)
    }

    val nextExercise = courseWithScore.upcomingExercises().firstOrNull { exercise ->
        val participation = exercise.latestParticipation
        val submission = participation?.submissions?.firstOrNull() ?: return@firstOrNull true
        val result = submission.results?.firstOrNull() ?: return@firstOrNull true
        result.successful == false
    }

    //if (isCompact) {
//        CompactCourseItemHeader(
//            modifier = modifier,
//            course = courseWithScore.course,
//            onClick = onClick,
//            compactCourseHeaderViewMode = CompactCourseHeaderViewMode.EXERCISE_AND_LECTURE_COUNT,
//            content = {

//                when (compactCourseHeaderViewMode) {
//                    CompactCourseHeaderViewMode.DESCRIPTION -> {
//                        Text(
//                            text = course.description,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f),
//                            color = MaterialTheme.colorScheme.secondary,
//                            fontSize = 12.sp,
//                            overflow = TextOverflow.Ellipsis,
//                            lineHeight = 14.sp,
//                            maxLines = 3
//                        )
//                    }
//
//                    CompactCourseHeaderViewMode.EXERCISE_AND_LECTURE_COUNT -> {
//                        CourseExerciseAndLectureCount(
//                            modifier = Modifier.fillMaxWidth(),
//                            exerciseCount = course.exercises.size,
//                            lectureCount = course.lectures.size,
//                            textStyle = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }

    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CourseItemHeader(
                modifier = Modifier,
                course = courseWithScore.course
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourseItemContent(
                    modifier = Modifier.weight(1f),
                    currentPointsFormatted = currentPointsFormatted,
                    maxPointsFormatted = maxPointsFormatted,
                    nextExercise = nextExercise
                )

                if (currentPoints > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .aspectRatio(1f)
                    ) {
                        CircularCourseProgress(
                            modifier = Modifier
                                .align(Alignment.Center),
                            progress = progress,
                            progressPercentFormatted = progressPercentFormatted
                        )
                    }
                }
            }
        }
    }
}
       // )
//    } else {
//        ExpandedCourseItemHeader(
//            modifier = modifier,
//            course = courseWithScore.course,
//            onClick = onClick,
//            content = {
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .aspectRatio(1f)
//                        .align(Alignment.CenterHorizontally)
//                ) {
//                    CircularCourseProgress(
//                        modifier = Modifier
//                            .fillMaxSize(0.8f)
//                            .align(Alignment.Center),
//                        progress = progress,
//                        currentPointsFormatted = currentPointsFormatted,
//                        maxPointsFormatted = maxPointsFormatted,
//                        progressPercentFormatted = progressPercentFormatted
//                    )
//                }
//
//                CourseExerciseAndLectureCount(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(vertical = 8.dp),
//                    exerciseCount = courseWithScore.course.exercises.size,
//                    lectureCount = courseWithScore.course.lectures.size,
//                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
//                    alignment = Alignment.CenterHorizontally
//                )
//            },
//            rightHeaderContent = { }
//        )
    //}

@Composable
private fun CourseItemHeader(
    modifier: Modifier,
    course: Course
) {
    val painter = getCourseIconPainter(course)

    val courseColor: Color = remember {
        try {
            Color(course.color?.toColorInt() ?: throw IllegalArgumentException())
        } catch (e: IllegalArgumentException) {
            CourseColors.artemisDefaultColor
        }
    }

    val courseImageModifier = Modifier
        .padding(start = 16.dp)
        .padding(vertical = 8.dp)
        .clip(CircleShape)
        .size(Spacings.CourseItem.headerHeight)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Spacings.CourseItem.headerHeight)
            .background(courseColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (painter != null) {
            Image(
                modifier = courseImageModifier,
                painter = painter,
                contentScale = ContentScale.FillBounds,
                contentDescription = null
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(start = 16.dp)
                    .size(Spacings.CourseItem.headerHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = course.title.first().toString(),
                    fontSize = 24.sp,
                )
            }
        }

        AutoResizeText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(end = 16.dp),
            text = course.title,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSizeRange = FontSizeRange(min = 14.sp, max = 20.sp),
            maxLines = 2
        )
    }
}

@Composable
private fun CourseItemContent(
    modifier: Modifier,
    currentPointsFormatted: String,
    maxPointsFormatted: String,
    nextExercise: Exercise?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.course_overview_course_score),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(
                    id = R.string.course_overview_course_progress_pts,
                    currentPointsFormatted,
                    maxPointsFormatted
                ),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column {
            Text(
                text = stringResource(R.string.course_overview_course_next_exercise),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = nextExercise?.title ?: stringResource(R.string.course_overview_course_no_exercise_planned),
                style = MaterialTheme.typography.bodyLarge,
                color = if (nextExercise == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CircularCourseProgress(
    modifier: Modifier,
    progress: Float,
    progressPercentFormatted: String
) {
    BoxWithConstraints(modifier = modifier) {
        val progressBarWidthDp = min(32.dp, maxWidth * 0.13f)
        val progressBarWidth = with(LocalDensity.current) { progressBarWidthDp.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(progressBarWidthDp)
        ) {
            drawArc(
                color = CourseColors.CircularCourseProgress.circle,
                startAngle = -90f + 360f * progress,
                sweepAngle = 360f * (1f - progress),
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )

            drawArc(
                color = CourseColors.CircularCourseProgress.track,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )
        }

        val percentFontSize = with(LocalDensity.current) {
            val availableSpace = maxHeight - progressBarWidthDp * 2
            (availableSpace * 0.25f).toSp()
        }

        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = stringResource(
                id = R.string.course_overview_course_progress_percentage,
                progressPercentFormatted
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontSize = percentFontSize,
            fontWeight = FontWeight.Normal
        )
    }
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