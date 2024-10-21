package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quizStatus

/**
 * Display a row of the categories this exercise is associated with
 */
@Composable
fun ExerciseCategoryChipRow(modifier: Modifier, exercise: Exercise) {
    val context = LocalContext.current

    val quizStatus = if (exercise is QuizExercise) {
        exercise.quizStatus.collectAsState(initial = QuizExercise.QuizStatus.INVISIBLE).value
    } else QuizExercise.QuizStatus.INVISIBLE

    val chips = remember(exercise, quizStatus) {
        collectExerciseCategoryChips(context, exercise, quizStatus)
    }

    ExerciseCategoryChipRow(modifier = modifier, chips = chips)
}

@Composable
fun ExerciseCategoryChipRow(modifier: Modifier, chips: List<ExerciseCategoryChipData>) {
    if (chips.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chipData ->
            ExerciseInfoChip(modifier = Modifier, data = chipData)
        }
    }
}

data class ExerciseCategoryChipData(val text: String, val color: Color)

/**
 * Displays a colored rounded rectangle with the given text in it.
 * These are not material chips, as material chips indicate an action that can be performed.
 */
@Composable
fun ExerciseInfoChip(modifier: Modifier, data: ExerciseCategoryChipData) {
    val color = data.color
    val text = data.text
    ExerciseInfoChip(modifier, color, text)
}

val ExerciseInfoChipTextHorizontalPadding = 8.dp
val ExerciseInfoChipTextVerticalPadding = 4.dp
val ExerciseInfoChipTextPaddingValues = PaddingValues(
    horizontal = ExerciseInfoChipTextHorizontalPadding,
    vertical = ExerciseInfoChipTextVerticalPadding
)

@Composable
fun ExerciseInfoChip(
    modifier: Modifier,
    color: Color,
    text: String
) {
    Box(
        modifier = modifier.then(
            Modifier.background(
                color = color,
                shape = RoundedCornerShape(25)
            )
        )
    ) {
        Text(
            modifier = Modifier.padding(ExerciseInfoChipTextPaddingValues),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
    }
}

/**
 * Generates a list of the chips that are displayed in the ui from the data available in the exercise.
 */
private fun collectExerciseCategoryChips(
    context: Context,
    exercise: Exercise,
    quizStatus: QuizExercise.QuizStatus
): List<ExerciseCategoryChipData> {
    val liveQuizChips =
        if (exercise is QuizExercise && quizStatus == QuizExercise.QuizStatus.ACTIVE)
            listOf(
                ExerciseCategoryChipData(
                    context.getString(de.tum.informatics.www1.artemis.native_app.core.ui.R.string.exercise_live_quiz),
                    Color(0xff28a745)
                )
            ) else emptyList()

    val bonusChips =
        if (exercise.includedInOverallScore == Exercise.IncludedInOverallScore.INCLUDED_AS_BONUS) {
            listOf(
                ExerciseCategoryChipData(
                    context.getString(de.tum.informatics.www1.artemis.native_app.core.ui.R.string.exercise_is_bonus),
                    Color.Cyan
                )
            )
        } else emptyList()

    val categoryChips = exercise.categories.map { category ->
        ExerciseCategoryChipData(
            category.category,
            Color(category.colorValue ?: 0xFFFFFFFF)
        )
    }

    return liveQuizChips + categoryChips + bonusChips
}