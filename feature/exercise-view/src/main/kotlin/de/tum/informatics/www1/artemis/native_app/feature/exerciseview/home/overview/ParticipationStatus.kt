package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.isParticipationAvailable
import de.tum.informatics.www1.artemis.native_app.core.ui.common.InfoMessageCard
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R

/**
 * Display the default participation status ui in combination with enhancing options.
 * These additional options are, e.g, a button to inspect the result.
 */
@Composable
internal fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise,
    showResult: Boolean = true,
    actions: ExerciseActions
) {
    ProvideDefaultExerciseTemplateStatus(exercise) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isParticipationAvailable = exercise.isParticipationAvailable.collectAsState(initial = false).value

            if (!isParticipationAvailable) {
                InfoMessageCard(
                    infoText = stringResource(de.tum.informatics.www1.artemis.native_app.core.ui.R.string.exercise_participation_not_possible),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    ExercisePointInfo(exercise)

                    ParticipationStatusUi(
                        modifier = Modifier,
                        exercise = exercise
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                ExerciseActionButtons(
                    modifier = Modifier,
                    exercise = exercise,
                    showResult = showResult,
                    actions = actions
                )
            }
        }
    }
}


@Composable
private fun ExercisePointInfo(exercise: Exercise) {
    val currentUserPoints = exercise.currentUserPoints.let(ExercisePointsDecimalFormat::format)
    val maxPoints = exercise.maxPoints?.let(ExercisePointsDecimalFormat::format)

    val pointsHintText = when {
        currentUserPoints != null && maxPoints != null -> stringResource(
            id = R.string.exercise_view_overview_points_reached,
            currentUserPoints,
            maxPoints
        )

        maxPoints != null -> stringResource(
            id = R.string.exercise_view_overview_points_max,
            maxPoints
        )

        else -> stringResource(id = R.string.exercise_view_overview_points_none)
    }

    Text(
        modifier = Modifier,
        text = pointsHintText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}