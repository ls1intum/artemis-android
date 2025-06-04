package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.isParticipationAvailable
import de.tum.informatics.www1.artemis.native_app.core.ui.common.InfoMessageCard
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus

/**
 * Display the default participation status ui in combination with enhancing options.
 * These additional options are, e.g, a button to inspect the result.
 */
@Composable
internal fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise
) {
    ProvideDefaultExerciseTemplateStatus(exercise) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isParticipationAvailable =
                exercise.isParticipationAvailable.collectAsState(initial = false).value

            if (!isParticipationAvailable) {
                InfoMessageCard(
                    infoText = stringResource(de.tum.informatics.www1.artemis.native_app.core.ui.R.string.exercise_participation_not_possible),
                )
            }
        }
    }
}
