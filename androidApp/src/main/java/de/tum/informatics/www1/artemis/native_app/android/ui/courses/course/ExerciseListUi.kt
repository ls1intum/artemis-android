package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.*
import de.tum.informatics.www1.artemis.native_app.android.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
fun ExerciseListUi(
    modifier: Modifier,
    exercisesDataState: DataState<List<WeeklyExercises>>,
    onClickExercise: (exerciseId: Int) -> Unit
) {
    EmptyDataStateUi(dataState = exercisesDataState) { weeklyExercises ->
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weeklyExercises.forEach { weeklyExercise ->
                item {
                    ExerciseWeekSectionHeader(
                        modifier = Modifier.fillMaxWidth(),
                        weeklyExercise
                    )
                }
            }
        }
    }
}

/**
 * Display a title with the time range of the week or a text indicating that no time is bound.
 */
@Composable
fun ExerciseWeekSectionHeader(modifier: Modifier, weeklyExercises: WeeklyExercises) {
    val text = when (weeklyExercises) {
        is WeeklyExercises.BoundToWeek -> {
            val (fromText, toText) = remember(
                weeklyExercises.firstDayOfWeek,
                weeklyExercises.lastDayOfWeek
            ) {
                val format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

                format.format(weeklyExercises.firstDayOfWeek) to format.format(weeklyExercises.lastDayOfWeek)
            }

            stringResource(id = R.string.course_ui_exercise_list_week_header, fromText, toText)
        }
        is WeeklyExercises.Unbound -> stringResource(id = R.string.course_ui_exercise_list_unbound_week_header)
    }


    Box(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
private fun Exercise(modifier: Modifier, exercise: Exercise, onClickExercise: () -> Unit) {
    val chips = remember(exercise) { collectExerciseCategoryChips(exercise) }

    Card(modifier = modifier, onClick = onClickExercise) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ExerciseTypeIcon(modifier = Modifier.size(80.dp), exercise = exercise)

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    text = exercise.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            //Display a row of chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {

            }
        }
    }
}

/**
 * Displays the icon of the exercise within an outlined circle
 */
@Composable
private fun ExerciseTypeIcon(modifier: Modifier, exercise: Exercise) {
    val icon = when (exercise) {
        is TextExercise -> Icons.Default.EditNote
        is ModelingExercise -> Icons.Default.AccountTree
        is FileUploadExercise -> Icons.Default.FileUpload
        is ProgrammingExercise -> Icons.Default.Code
        is UnknownExercise -> Icons.Default.QuestionMark
    }

    Box(
        modifier = modifier.then(
            Modifier
                .border(width = Dp.Hairline, color = LocalContentColor.current, shape = CircleShape)
        )
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            imageVector = icon,
            contentDescription = null
        )
    }
}

private data class ExerciseCategoryChipData(val text: String, val color: Color)

/**
 * Displays a colored rounded rectangle with the given text in it.
 * These are not material chips, as material chips indicate an action that can be performed.
 */
@Composable
private fun ExerciseCategoryChip(modifier: Modifier, data: ExerciseCategoryChipData) {
    Box(modifier = modifier.then(Modifier.background(color = data.color, shape = CircleShape))) {
        Text(
            modifier = Modifier.padding(2.dp),
            text = data.text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun collectExerciseCategoryChips(
    context: Context,
    exercise: Exercise
): List<ExerciseCategoryChipData> {
    val difficulty = exercise.difficulty
    val difficultyChips = if (difficulty != null) {
        val data = when (difficulty) {
            Exercise.Difficulty.EASY ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_easy),
                    Color.Green
                )
            Exercise.Difficulty.MEDIUM ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_medium),
                    Color.Yellow
                )
            Exercise.Difficulty.HARD ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_hard),
                    Color.Red
                )
        }
        listOf(data)
    } else emptyList()

    val bonusChips =
        if (exercise.includedInOverallScore == Exercise.IncludedInOverallScore.INCLUDED_AS_BONUS) {
            listOf(
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_is_bonus),
                    Color.Cyan
                )
            )
        } else emptyList()

    val categoryChips = exercise.categories.map { category ->
        ExerciseCategoryChipData(
            category.category,
            category.color ?: Color.White
        )
    }

    return difficultyChips + bonusChips + categoryChips
}