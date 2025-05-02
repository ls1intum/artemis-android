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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.color
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.label
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.status
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private data class TwoLineChipData(val title: String, val value: String, val isWarning: Boolean = false)
private data class DifficultyChipData(val difficulty: Exercise.Difficulty)
private data class CategoryChipData(val badges: List<SpecialBadgeData>)
private data class SpecialBadgeData(val text: String, val background: Color)

private val ChipHeight = 50.dp
private val ChipCorner = 8.dp
private val ChipBorder = 1.dp
private val ChipPadding = 8.dp

@Composable
fun ExerciseChips(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {

    val mainChips = buildList {
        // Points
        exercise.maxPoints?.let { max ->
            val currentUserPoints = exercise.currentUserPoints.let(ExercisePointsDecimalFormat::format)
            val maxPoints = max.let(ExercisePointsDecimalFormat::format)
            add(
                TwoLineChipData(
                    title = stringResource(R.string.points_title),
                    value = stringResource(
                        id = R.string.exercise_points,
                        currentUserPoints,
                        maxPoints
                    )
                )
            )
        }

        // Submission
        exercise.dueDate?.let { due ->
            val now = Clock.System.now()
            val remaining = due.epochSeconds - now.epochSeconds
            val isFuture = remaining > 0
            val isLessThanWeek = kotlin.math.abs(remaining) < 7 * 24 * 60 * 60
            val isLessThanDay = remaining in 1..86_400

            val value: String = if (isFuture && isLessThanWeek) {
                DateUtils.getRelativeTimeSpanString(
                    due.toEpochMilliseconds(),
                    now.toEpochMilliseconds(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } else {
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                due.toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
                    .format(formatter)
            }

            val title = if (remaining > 0) R.string.submission_due_title else R.string.submission_closed_title
            add(
                TwoLineChipData(
                    title = stringResource(title),
                    value = value,
                    isWarning = isLessThanDay
                )
            )
        }

        // Status
        add(
            TwoLineChipData(
                title = stringResource(R.string.status_title),
                value = stringResource(exercise.status)
            )
        )
    }

   //Difficulty
    val difficultyChip = exercise.difficulty?.let { DifficultyChipData(it) }

   //Category
    val badgeData = buildList {
        // Not released yet
        exercise.releaseDate?.takeIf { it > Clock.System.now() }?.let {
            add(SpecialBadgeData(stringResource(R.string.not_released), Color(0xFFFFA500)))
        }
        // Included in overall score
        if (exercise.includedInOverallScore != Exercise.IncludedInOverallScore.INCLUDED_COMPLETELY) {
            add(
                SpecialBadgeData(
                    stringResource(exercise.includedInOverallScore.label),
                    exercise.includedInOverallScore.color
                )
            )
        }
        // Categories (max 2 + remainder)
        exercise.categories.take(2).forEach { cat ->
            add(
                SpecialBadgeData(
                    cat.category,
                    cat.colorValue?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                )
            )
        }
        val remainder = exercise.categories.size - 2
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
        items(mainChips) { data -> MainChip(data) }

        // difficulty
        difficultyChip?.let { item { DifficultyChip(it) } }

        // category badges
        categoryChip?.let { item { CategoryChip(it) } }
    }
}


@Composable
private fun MainChip(data: TwoLineChipData) {
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
                text = data.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = data.value,
                style = MaterialTheme.typography.labelMedium,
                color = if (data.isWarning) Color.Red else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                text = stringResource(R.string.difficulty_title),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
                text = stringResource(R.string.category_title),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
            style = MaterialTheme.typography.labelSmall,
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
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { idx ->
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = if (idx < fill) difficulty.color else difficulty.color.copy(alpha = 0.3f)
            ) { Spacer(modifier = Modifier.size(width = 20.dp, height = 10.dp)) }
        }
    }
}
