package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.R

/**
 * Display the participation status in a human readable form.
 * The status is displayed a a row.
 */
@Composable
fun ParticipationStatusUi(
    exercise: Exercise,
    participationStatus: Exercise.ParticipationStatus,
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
    when (participationStatus) {
        is Exercise.ParticipationStatus.ParticipationStatusWithParticipation -> {
            //Display dynamic updates component
            val templateStatus = getTemplateStatus(participationStatus.participation)

            ExerciseResult(
                modifier = Modifier.fillMaxWidth(),
                showUngradedResults = true,
                templateStatus = templateStatus
            )
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


//From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/submission-result-status.component.html
@Composable
private fun getSubmissionResultStatusText(participationStatus: Exercise.ParticipationStatus): String {
    val id = when (participationStatus) {
        Exercise.ParticipationStatus.QuizNotInitialized -> R.string.exercise_quiz_not_started
        Exercise.ParticipationStatus.QuizActive -> R.string.exercise_user_participating
        Exercise.ParticipationStatus.QuizSubmitted -> R.string.exercise_user_submitted
        Exercise.ParticipationStatus.QuizNotStarted -> R.string.exercise_quiz_not_started
        Exercise.ParticipationStatus.QuizNotParticipated -> R.string.exercise_user_not_participated
        Exercise.ParticipationStatus.NoTeamAssigned -> R.string.exercise_user_not_assigned_to_team
        Exercise.ParticipationStatus.Uninitialized -> R.string.exercise_user_not_started_exercise
        Exercise.ParticipationStatus.ExerciseActive -> R.string.exercise_exercise_not_submitted
        Exercise.ParticipationStatus.ExerciseMissed -> R.string.exercise_exercise_missed_deadline
        else -> 0
    }

    return stringResource(id = id)
}