package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.isUninitialized
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.latestParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.notStarted
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation.InitializationState
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed

/**
 * Display the participation status in a human readable form.
 * The status is displayed a a row.
 */
@Composable
fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise
) {
    val participation = remember(exercise) { exercise.latestParticipation }

    if (participation != null && participation.results.orEmpty().isNotEmpty()) {
        // Display dynamic updates component
        ExerciseResult(
            modifier = modifier,
            showUngradedResults = true,
            exercise = exercise
        )
    } else {
        // Simply display text
        Text(
            modifier = modifier,
            text = getSubmissionResultStatusText(
                participation = participation,
                exercise = exercise
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


//From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/submission-result-status.component.html
@Composable
private fun getSubmissionResultStatusText(
    participation: Participation?,
    exercise: Exercise
): String {
    val isAfterDueDate = exercise.dueDate?.hasPassed() ?: false
    val exerciseMissedDeadline = isAfterDueDate && exercise.latestParticipation == null

    val uninitialized =
        if (exercise is QuizExercise) exercise.isUninitializedC else !isAfterDueDate && participation == null

    val notSubmitted =
        !isAfterDueDate && participation != null && participation.submissions.orEmpty()
            .isEmpty()

    val id = when {
        // TODO: Automatically update using a websocket.
        exercise.teamMode && exercise.studentAssignedTeamIdComputed && exercise.studentAssignedTeamId != null -> {
            R.string.exercise_user_not_assigned_to_team
        }

        uninitialized -> R.string.exercise_user_not_started_exercise
        exerciseMissedDeadline -> R.string.exercise_exercise_missed_deadline
        notSubmitted -> R.string.exercise_exercise_not_submitted
        participation?.initializationState == InitializationState.FINISHED -> R.string.exercise_user_submitted
        participation?.initializationState == InitializationState.INITIALIZED && exercise is QuizExercise -> R.string.exercise_user_participating
        exercise is QuizExercise && exercise.notStartedC -> R.string.exercise_quiz_not_started
        else -> R.string.exercise_user_unknown
    }

    return stringResource(id = id)
}

val QuizExercise.isUninitializedC: Boolean
    @Composable get() = isUninitialized.collectAsState(initial = false).value

val QuizExercise.notStartedC: Boolean
    @Composable get() = notStarted.collectAsState(initial = false).value