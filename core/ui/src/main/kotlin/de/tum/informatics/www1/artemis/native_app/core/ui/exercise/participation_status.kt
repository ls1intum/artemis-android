package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.isUninitialized
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.notStarted
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed

/**
 * Display the participation status in a human readable form.
 * The status is displayed a a row.
 */
@Composable
fun ParticipationStatusUi(
    exercise: Exercise,
    getTemplateStatus: @Composable (Participation) -> ResultTemplateStatus = { participation ->
        computeTemplateStatus(
            exercise = exercise,
            participation = participation,
            result = null,
            showUngradedResults = true,
            personal = true
        ).collectAsState(initial = ResultTemplateStatus.NoResult).value
    }
) {
    if (exercise.studentParticipations.orEmpty().isNotEmpty()) {
        //Display dynamic updates component
        val templateStatus = getTemplateStatus(exercise.studentParticipations!!.first())

        ExerciseResult(
            modifier = Modifier.fillMaxWidth(),
            showUngradedResults = true,
            templateStatus = templateStatus,
            exercise = exercise
        )
    } else {
        //Simply display text
        Text(
            text = getSubmissionResultStatusText(exercise = exercise),
            style = MaterialTheme.typography.labelLarge
        )
    }
}


//From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/submission-result-status.component.html
@Composable
private fun getSubmissionResultStatusText(exercise: Exercise): String {
    val isAfterDueDate = exercise.dueDate?.hasPassed() ?: false
    val uninitialized = !isAfterDueDate && exercise.studentParticipations.orEmpty().isNotEmpty()
    val notSubmitted = !isAfterDueDate && exercise.studentParticipations.isNullOrEmpty()

    val isUserParticipating = exercise.studentParticipations.orEmpty()
        .firstOrNull()?.initializationState == Participation.InitializationState.INITIALIZED
    val hasUserSubmitted = exercise.studentParticipations.orEmpty()
        .firstOrNull()?.initializationState == Participation.InitializationState.FINISHED

    val id = when {
        // TODO: Implement teams
//        exercise.teamMode == true && exercise.studentAssignedTeamIdComputed && exercise.studentAssignedTeamId != null -> {
//            R.string.exercise_user_not_assigned_to_team
//        }
        uninitialized -> R.string.exercise_user_not_started_exercise
        isAfterDueDate -> R.string.exercise_exercise_missed_deadline
        notSubmitted -> R.string.exercise_exercise_not_submitted
        exercise is QuizExercise && exercise.isUninitializedC -> R.string.exercise_quiz_not_started
        isUserParticipating -> R.string.exercise_user_participating
        hasUserSubmitted -> R.string.exercise_user_submitted
        exercise is QuizExercise && exercise.notStartedC -> R.string.exercise_quiz_not_started
        else -> 0
    }

    return stringResource(id = id)
}

private val QuizExercise.isUninitializedC: Boolean
    @Composable get() = isUninitialized.collectAsState(initial = false).value

private val QuizExercise.notStartedC: Boolean
    @Composable get() = notStarted.collectAsState(initial = false).value