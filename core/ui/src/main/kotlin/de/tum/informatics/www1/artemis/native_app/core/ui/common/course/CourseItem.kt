package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.latestParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.upcomingExercises
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.util.CourseUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.BackgroundColorBasedTextColor
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.CoursePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.CourseColors
import java.text.DecimalFormat

const val TEST_TAG_ENROLL_BUTTON = "TEST_TAG_ENROLL_BUTTON"
private const val progressFontSizeMultiplier = 0.25f

/**
 * Displays course card with score and exercise information for the dashboard.
 */
@Composable
fun CourseItem(
    modifier: Modifier,
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

    val progress = if (maxPoints == 0f) 0f else (currentPoints / maxPoints).coerceIn(0f, 1f)
    val progressPercentFormatted = remember(progress) {
        DecimalFormat("0.##%").format(progress)
    }

    val nextExercise = courseWithScore.upcomingExercises().firstOrNull { exercise ->
        val participation = exercise.latestParticipation
        val submission = participation?.submissions?.firstOrNull() ?: return@firstOrNull true
        val result = submission.results?.firstOrNull() ?: return@firstOrNull true
        result.successful == false
    }

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
                    nextExercise = nextExercise,
                    courseId = courseWithScore.course.id ?: 0L
                )

                if (currentPoints > 0f) {
                    val isTablet = getArtemisAppLayout() == ArtemisAppLayout.Tablet

                    Box(
                        modifier = Modifier
                            .width(if (isTablet) 150.dp else 120.dp)
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

/**
 * Displays the course preview card for the registration screen.
 */
@Composable
fun CourseItemPreview(
    modifier: Modifier,
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CourseItemHeader(
                modifier = Modifier,
                course = course,
                height = Spacings.CourseItem.previewHeaderHeight
            )

            if (course.description.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp),
                    text = course.description,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .testTag(TEST_TAG_ENROLL_BUTTON),
                onClick = onClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(id = R.drawable.enroll),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = stringResource(id = R.string.course_registration_sign_up))
                }
            }
        }
    }
}

@Composable
private fun CourseItemHeader(
    modifier: Modifier,
    course: Course,
    height: Dp = Spacings.CourseItem.headerHeight
) {
    val painter = CourseUtil.getCourseIconPainter(course)

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
        .size(height)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
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
                    .size(height)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = course.title.first().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        AutoResizeText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(end = 16.dp),
            text = course.title,
            color = BackgroundColorBasedTextColor.of(courseColor),
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
    nextExercise: Exercise?,
    courseId: Long
) {
    val localLinkOpener = LocalLinkOpener.current

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
                modifier = Modifier.let {
                    if (nextExercise != null) it.clickable {
                        val url = ExerciseDeeplinks.ToExercise.inAppLink(courseId, nextExercise.id ?: 0L)
                        localLinkOpener.openLink(url)
                    }
                    else it
                },
                text = nextExercise?.title ?: stringResource(R.string.course_overview_course_no_exercise_planned),
                style = MaterialTheme.typography.bodyLarge,
                color = if (nextExercise == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            (availableSpace * progressFontSizeMultiplier).toSp()
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