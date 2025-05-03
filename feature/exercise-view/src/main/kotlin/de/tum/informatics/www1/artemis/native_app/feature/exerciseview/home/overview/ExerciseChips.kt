package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.label
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.days

private const val MAX_NUMBER_OF_CHIPS = 2

private sealed interface OverviewChipData {
    val title: String
}
data class TextChip(
    val value: String,
    val isWarning: Boolean = false, override val title: String
) : OverviewChipData

data class ContentChip(
    override val title: String,
    val content: @Composable () -> Unit
) : OverviewChipData

private data class DifficultyChipData(val difficulty: Exercise.Difficulty)
private data class CategoryChipData(val badges: List<SpecialBadgeData>)
private data class SpecialBadgeData(val text: String, val background: Color)

private val ChipHeight = 50.dp
private val ChipCorner = 8.dp
private val ChipBorder = 1.dp
private val ChipPadding = 8.dp

@Composable
fun ExerciseOverviewChips(
    modifier: Modifier = Modifier,
    exercise: Exercise
) {

    val mainChips = buildList {
        // Points
        exercise.maxPoints?.let { max ->
            val currentUserPoints =
                exercise.currentUserPoints.let(ExercisePointsDecimalFormat::format)
            val maxPoints = max.let(ExercisePointsDecimalFormat::format)

            val pointsText = when {
                currentUserPoints != null && maxPoints != null -> stringResource(
                    id = R.string.exercise_chips_exercise_points,
                    currentUserPoints,
                    maxPoints
                )

                maxPoints != null -> stringResource(
                    id = R.string.exercise_chips_points_max,
                    maxPoints
                )

                else -> stringResource(id = R.string.exercise_chips_points_none)
            }

            add(
                TextChip(
                    title = stringResource(R.string.exercise_chips_points_title),
                    value = pointsText
                )
            )
        }

        exercise.dueDate?.let { due ->
            val now = Clock.System.now()
            val remaining = due - now

            val isLessThanDay = remaining.isPositive() && remaining < 1.days

            val value = getFormattedSubmissionTime(due)

            val title = if (remaining.isPositive()) {
                R.string.exercise_chips_submission_due_title
            } else {
                R.string.exercise_chips_submission_closed_title
            }

            add(
                TextChip(
                    title = stringResource(title),
                    value = value ?: "",
                    isWarning = isLessThanDay
                )
            )
        }

        // Status
        add(
            ContentChip(
                title = stringResource(R.string.exercise_chips_status_title),
                content = {
                    ProvideDefaultExerciseTemplateStatus(exercise) {
                        ParticipationStatusUi(
                            modifier = Modifier,
                            exercise = exercise,
                            isChip = true
                        )
                    }
                }
            )
        )
    }

    // Difficulty
    val difficultyChip = exercise.difficulty?.let { DifficultyChipData(it) }

    //Category
    val badgeData = buildList {
        // Not released yet
        exercise.releaseDate?.takeIf { it > Clock.System.now() }?.let {
            add(
                SpecialBadgeData(
                    stringResource(R.string.exercise_chips_not_released),
                    ExerciseColors.Category.notReleased
                )
            )
        }
        // Included in overall score
        if (exercise.includedInOverallScore != Exercise.IncludedInOverallScore.INCLUDED_COMPLETELY) {
            add(
                SpecialBadgeData(
                    stringResource(exercise.includedInOverallScore.label),
                    ExerciseColors.Type.notIncluded
                )
            )
        }
        // Categories (max 2 + remainder)
        exercise.categories.take(MAX_NUMBER_OF_CHIPS).forEach { cat ->
            add(
                SpecialBadgeData(
                    cat.category,
                    cat.colorValue?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface
                )
            )
        }
        val remainder = exercise.categories.size - MAX_NUMBER_OF_CHIPS
        if (remainder > 0) add(SpecialBadgeData("+$remainder more", Color.LightGray))
    }
    val categoryChip = badgeData.takeIf { it.isNotEmpty() }?.let { CategoryChipData(it) }


    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // main two‑line chips
        items(mainChips) { data -> OverviewChipItem(data) }

        // Difficulty
        difficultyChip?.let { item { DifficultyChip(it) } }

        // Category badges
        categoryChip?.let { item { CategoryChip(it) } }
    }
}


@Composable
private fun OverviewChipItem(data: OverviewChipData) {
    Surface(
        modifier = Modifier.height(ChipHeight),
        shape = RoundedCornerShape(ChipCorner),
        border = BorderStroke(ChipBorder, color = Color.Gray),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(ChipPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            when (data) {
                is TextChip -> {
                    Text(
                        text = data.value,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (data.isWarning) Color.Red else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                is ContentChip -> {
                    data.content()
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(data: DifficultyChipData) {
    Surface(
        modifier = Modifier
            .height(ChipHeight),
        shape = RoundedCornerShape(ChipCorner),
        border = BorderStroke(ChipBorder, color = Color.Gray),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(ChipPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.exercise_chips_difficulty_title),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            DifficultyBar(data.difficulty)
        }
    }
}

@Composable
private fun CategoryChip(data: CategoryChipData) {
    Surface(
        modifier = Modifier
            .height(ChipHeight),
        shape = RoundedCornerShape(ChipCorner),
        border = BorderStroke(ChipBorder, color = Color.Gray),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(ChipPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.exercise_chips_category_title),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                data.badges.forEach { SpecialBadge(it) }
            }
        }
    }
}

@Composable
private fun SpecialBadge(badge: SpecialBadgeData) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badge.background,
        contentColor = Color.White
    ) {
        Text(
            text = badge.text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DifficultyBar(difficulty: Exercise.Difficulty) {
    val fill = when (difficulty) {
        Exercise.Difficulty.EASY -> 1
        Exercise.Difficulty.MEDIUM -> 2
        Exercise.Difficulty.HARD -> 3
    }
    val color = when (difficulty) {
        Exercise.Difficulty.EASY -> ExerciseColors.Difficulty.easy
        Exercise.Difficulty.MEDIUM -> ExerciseColors.Difficulty.medium
        Exercise.Difficulty.HARD -> ExerciseColors.Difficulty.hard
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { idx ->
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = if (idx < fill) color else color.copy(alpha = 0.3f)
            ) {
                Spacer(modifier = Modifier.size(width = 20.dp, height = 10.dp))
            }
        }
    }
}

@Composable
fun getFormattedSubmissionTime(dueDate: Instant?): String? {
    dueDate ?: return null

    val now = Clock.System.now()
    val remaining = dueDate - now

    val isFuture = remaining.isPositive()
    val isLessThanWeek = remaining.absoluteValue < 7.days

    return if (isFuture && isLessThanWeek) {
        DateUtils.getRelativeTimeSpanString(
            dueDate.toEpochMilliseconds(),
            now.toEpochMilliseconds(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    } else {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        dueDate.toLocalDateTime(TimeZone.currentSystemDefault())
            .toJavaLocalDateTime()
            .format(formatter)
    }
}
