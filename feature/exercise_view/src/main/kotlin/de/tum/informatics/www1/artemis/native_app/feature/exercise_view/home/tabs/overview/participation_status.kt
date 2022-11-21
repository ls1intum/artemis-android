package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.ProgrammingExerciseStudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ResultTemplateStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.computeTemplateStatus
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

/**
 * Display the default participation status ui in combination with enhancing options.
 * These additional options are, e.g, a button to inspect the result.
 * @param viewResultInformation called when the user wants to view their latest result.
 */
@Composable
internal fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise,
    viewResultInformation: () -> Unit
) {
    val participationStatus = remember(exercise) {
        exercise.computeParticipationStatus(null)
    }

    val templateStatus: ResultTemplateStatus? = when (participationStatus) {
        is Exercise.ParticipationStatus.ParticipationStatusWithParticipation -> {
            computeTemplateStatus(
                exercise = exercise,
                participation = participationStatus.participation,
                result = null,
                showUngradedResults = true,
                personal = true
            ).collectAsState(initial = ResultTemplateStatus.NoResult).value
        }
        else -> null
    }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.exercise_participation_status_title),
                style = MaterialTheme.typography.titleSmall
            )

            ParticipationStatusUi(
                exercise = exercise,
                participationStatus = participationStatus,
                getTemplateStatus = {
                    checkNotNull(templateStatus) { "template status must not be null as participation status has a participation" }
                    templateStatus
                }
            )

            if (templateStatus != null) {
                if (templateStatus is ResultTemplateStatus.WithResult && canShowResultDetails(
                        null,
                        templateStatus.result
                    )
                ) {
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = viewResultInformation
                    ) {
                        Text(text = stringResource(id = R.string.exercise_participation_status_view_result_button))
                    }
                }
            }
        }
    }
}

private fun canShowResultDetails(@Suppress("SameParameterValue") submission: Submission?, result: Result): Boolean {
    if (result.isPreliminary) return true

    if (result.submission != null && submission is ProgrammingSubmission && submission.buildFailed == true) return true
    return result.hasFeedback == true
}