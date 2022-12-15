package de.tum.informatics.www1.artemis.native_app.feature.course_view

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.text.SimpleDateFormat
import java.util.*

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
internal fun ExerciseListUi(
    modifier: Modifier,
    exercisesDataState: DataState<List<WeeklyExercises>>,
    onClickExercise: (exerciseId: Long) -> Unit
) {
    EmptyDataStateUi(dataState = exercisesDataState) { weeklyExercises ->
        val weeklyExercisesExpanded: MutableMap<WeeklyExercises, Boolean> = remember(
            weeklyExercises
        ) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            SnapshotStateMap<WeeklyExercises, Boolean>().apply {
                putAll(
                    weeklyExercises
                        .map {
                            it to when (it) {
                                is WeeklyExercises.Unbound -> true
                                is WeeklyExercises.BoundToWeek -> {
                                    it.firstDayOfWeek.daysUntil(today) < 14
                                }
                            }
                        }
                )
            }
        }

        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weeklyExercises.forEach { weeklyExercise ->
                item {
                    ExerciseWeekSectionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                weeklyExercisesExpanded[weeklyExercise] =
                                    weeklyExercisesExpanded[weeklyExercise] != true
                            })
                            .padding(horizontal = 16.dp),
                        weeklyExercises = weeklyExercise,
                        expanded = weeklyExercisesExpanded[weeklyExercise] == true,
                    )
                }

                if (weeklyExercisesExpanded[weeklyExercise] == true) {
                    items(weeklyExercise.exercises, key = { it.id ?: it.hashCode() }) { exercise ->
                        ExerciseItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            exercise = exercise
                        ) { onClickExercise(exercise.id ?: return@ExerciseItem) }
                    }
                }

                item { Divider() }
            }
        }
    }
}

/**
 * Display a title with the time range of the week or a text indicating that no time is bound.
 * Displays an icon button that lets the user expand and collapse the weekly exercises
 * @param expanded if the exercise group this is showing is expanded
 */
@Composable
private fun ExerciseWeekSectionHeader(
    modifier: Modifier,
    weeklyExercises: WeeklyExercises,
    expanded: Boolean
) {
    val text = when (weeklyExercises) {
        is WeeklyExercises.BoundToWeek -> {
            val (fromText, toText) = remember(
                weeklyExercises.firstDayOfWeek,
                weeklyExercises.lastDayOfWeek
            ) {
                val format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

                val dateToInstant = { date: LocalDate ->
                    Date.from(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toJavaInstant())
                }

                val fromDate = dateToInstant(weeklyExercises.firstDayOfWeek)
                val toData = dateToInstant(weeklyExercises.lastDayOfWeek)

                format.format(fromDate) to format.format(toData)
            }

            stringResource(id = R.string.course_ui_exercise_list_week_header, fromText, toText)
        }
        is WeeklyExercises.Unbound -> stringResource(id = R.string.course_ui_exercise_list_unbound_week_header)
    }


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.titleMedium
        )

        val icon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        val contentDescription =
            stringResource(
                id = if (expanded) R.string.course_ui_exercise_list_expand_button_less_content_info
                else R.string.course_ui_exercise_list_expand_button_more_content_info
            )

        Icon(icon, contentDescription)
    }
}

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
private fun ExerciseItem(
    modifier: Modifier,
    exercise: Exercise,
    onClickExercise: () -> Unit
) {
    Card(modifier = modifier, onClick = onClickExercise) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ExerciseTypeIcon(modifier = Modifier.size(80.dp), exercise = exercise)

                ExerciseDataText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    exercise = exercise
                )
            }

            //Display a row of chips
            ExerciseCategoryChipRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                exercise = exercise
            )
        }
    }
}

/**
 * Displays the icon of the exercise within an outlined circle
 */
@Composable
private fun ExerciseTypeIcon(modifier: Modifier, exercise: Exercise) {
    Box(
        modifier = modifier.then(
            Modifier
                .border(width = 1.dp, color = LocalContentColor.current, shape = CircleShape)
        )
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = getExerciseTypeIcon(exercise),
            contentDescription = null
        )
    }
}

/**
 * Displays the exercise title, the due data and the participation info. The participation info is automatically updated.
 */
@Composable
private fun ExerciseDataText(modifier: Modifier, exercise: Exercise) {
    //Format a relative time if the distant is
    val formattedDueDate = remember(exercise) {
        val dueDate = exercise.dueDate
        if (dueDate != null) {
            DateUtils
                .getRelativeTimeSpanString(
                    dueDate.toEpochMilliseconds(),
                    Clock.System.now().toEpochMilliseconds(),
                    0L,
                    DateUtils.FORMAT_ABBREV_ALL
                )
        } else null
    }

    Column(modifier = modifier) {
        Text(
            text = exercise.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text =
            if (formattedDueDate != null) stringResource(
                id = R.string.course_ui_exercise_item_due_date_set,
                formattedDueDate
            ) else stringResource(id = R.string.course_ui_exercise_item_due_date_not_set),
            style = MaterialTheme.typography.bodyMedium
        )

        ParticipationStatusUi(exercise = exercise)
    }
}