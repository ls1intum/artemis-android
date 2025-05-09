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
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
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

sealed interface OverviewChipData {
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

data class DifficultyChipData(val difficulty: Exercise.Difficulty)
data class CategoryChipData(val badges: List<SpecialBadgeData>)
data class SpecialBadgeData(val text: String, val background: Color)

@Composable
fun ExerciseOverviewChips(
    modifier: Modifier = Modifier,
    exercise: Exercise
) {
    val mainChips = ExerciseOverviewChipUtil.buildMainChips(exercise)
    val difficultyChip = ExerciseOverviewChipUtil.buildDifficultyChip(exercise)
    val categoryChip = ExerciseOverviewChipUtil.buildCategoryChip(exercise)

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(mainChips) { data -> OverviewChipItem(data) }
        difficultyChip?.let { item { DifficultyChip(it) } }
        categoryChip?.let { item { CategoryChip(it) } }
    }
}

@Composable
private fun OverviewChipItem(data: OverviewChipData) {
    OverviewChipContainer(title = data.title) {
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


@Composable
private fun OverviewChipContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.height(Spacings.ExerciseOverviewChips.height),
        shape = RoundedCornerShape(Spacings.ExerciseOverviewChips.corner),
        border = BorderStroke(Spacings.ExerciseOverviewChips.border, color = MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(Spacings.ExerciseOverviewChips.padding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            content()
        }
    }
}


@Composable
private fun DifficultyChip(data: DifficultyChipData) {
    OverviewChipContainer(title = stringResource(R.string.exercise_chips_difficulty_title)) {
        DifficultyBar(data.difficulty)
    }
}

@Composable
private fun CategoryChip(data: CategoryChipData) {
    OverviewChipContainer(title = stringResource(R.string.exercise_chips_category_title)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            data.badges.forEach { SpecialBadge(it) }
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
