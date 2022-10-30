package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.*
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ExerciseService
import de.tum.informatics.www1.artemis.native_app.android.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import okhttp3.internal.format
import org.koin.androidx.compose.get
import java.text.SimpleDateFormat
import java.util.*

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
fun ExerciseListUi(
    modifier: Modifier,
    exercisesDataState: DataState<List<WeeklyExercises>>,
    loadExerciseDetails: (exerciseId: Int) -> Flow<DataState<Exercise>>,
    onClickExercise: (exerciseId: Int) -> Unit
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
                        Exercise(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            exercise = exercise,
                            loadExerciseDetails = loadExerciseDetails,
                            onClickExercise = { onClickExercise(exercise.id ?: return@Exercise) }
                        )
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
fun ExerciseWeekSectionHeader(
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
 *
 * Additionally, loads the exercise details from the server. Once loaded, replaces the supplied exercise with the loaded one.
 *
 * @param loadExerciseDetails supply a flow that returns the exercise details.
 */
@Composable
private fun Exercise(
    modifier: Modifier,
    exercise: Exercise,
    loadExerciseDetails: (exerciseId: Int) -> Flow<DataState<Exercise>>,
    onClickExercise: () -> Unit
) {
//    val loadedExercise by flow {
//        val id = exercise.id
//        if (id != null) {
//            emitAll(loadExerciseDetails(id))
//        }
//
//    }.collectAsState(initial = DataState.Loading())

    val actExercise = exercise

    val context = LocalContext.current
    val chips = remember(actExercise) { collectExerciseCategoryChips(context, actExercise) }

    Card(modifier = modifier, onClick = onClickExercise) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ExerciseTypeIcon(modifier = Modifier.size(80.dp), exercise = actExercise)

                ExerciseDataText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    exercise = actExercise
                )
            }

            //Display a row of chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chipData ->
                    ExerciseCategoryChip(modifier = Modifier, data = chipData)
                }
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
        else -> Icons.Default.QuestionMark
    }

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
            imageVector = icon,
            contentDescription = null
        )
    }
}

/**
 * Displays the exercise title, the due data and the participation info. The participation info is automatically updates.
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

        when (val participationStatus = exercise.computeParticipationStatus(testRun = null)) {
            Exercise.ParticipationStatus.QUIZ_FINISHED,
            Exercise.ParticipationStatus.INACTIVE,
            Exercise.ParticipationStatus.INITIALIZED,
            Exercise.ParticipationStatus.EXERCISE_SUBMITTED -> {
                //Display dynamic updates component
                Exercise
            }
            else -> {
                //Simply display text
                Text(
                    text = getSubmissionResultStatusText(participationStatus = participationStatus),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

//From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/submission-result-status.component.html
@Composable
private fun getSubmissionResultStatusText(participationStatus: Exercise.ParticipationStatus): String {
    val id = when (participationStatus) {
        Exercise.ParticipationStatus.QUIZ_UNINITIALIZED -> R.string.exercise_quiz_not_started
        Exercise.ParticipationStatus.QUIZ_ACTIVE -> R.string.exercise_user_participating
        Exercise.ParticipationStatus.QUIZ_SUBMITTED -> R.string.exercise_user_submitted
        Exercise.ParticipationStatus.QUIZ_NOT_STARTED -> R.string.exercise_quiz_not_started
        Exercise.ParticipationStatus.QUIZ_NOT_PARTICIPATED -> R.string.exercise_user_not_participated
        Exercise.ParticipationStatus.NO_TEAM_ASSIGNED -> R.string.exercise_user_not_assigned_to_team
        Exercise.ParticipationStatus.UNINITIALIZED -> R.string.exercise_user_not_started_exercise
        Exercise.ParticipationStatus.EXERCISE_ACTIVE -> R.string.exercise_exercise_not_submitted
        Exercise.ParticipationStatus.EXERCISE_MISSED -> R.string.exercise_exercise_missed_deadline
        Exercise.ParticipationStatus.QUIZ_FINISHED,
        Exercise.ParticipationStatus.INACTIVE,
        Exercise.ParticipationStatus.INITIALIZED,
        Exercise.ParticipationStatus.EXERCISE_SUBMITTED -> 0
    }

    return stringResource(id = id)
}

private data class ExerciseCategoryChipData(val text: String, val color: Color)

/**
 * Displays a colored rounded rectangle with the given text in it.
 * These are not material chips, as material chips indicate an action that can be performed.
 */
@Composable
private fun ExerciseCategoryChip(modifier: Modifier, data: ExerciseCategoryChipData) {
    Box(
        modifier = modifier.then(
            Modifier.background(
                color = data.color,
                shape = RoundedCornerShape(25)
            )
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            text = data.text,
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
    exercise: Exercise
): List<ExerciseCategoryChipData> {
    val liveQuizChips =
        if (exercise is QuizExercise && exercise.status == QuizExercise.QuizStatus.ACTIVE)
            listOf(
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_live_quit),
                    Color(0xff28a745)
                )
            ) else emptyList()

    val difficulty = exercise.difficulty
    val difficultyChips = if (difficulty != null) {
        val data = when (difficulty) {
            Exercise.Difficulty.EASY ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_easy),
                    Color(0xff28a745)
                )
            Exercise.Difficulty.MEDIUM ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_medium),
                    Color(0xffffc107)
                )
            Exercise.Difficulty.HARD ->
                ExerciseCategoryChipData(
                    context.getString(R.string.exercise_difficulty_hard),
                    Color(0xffdc3545)
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

    return liveQuizChips + categoryChips + difficultyChips + bonusChips
}