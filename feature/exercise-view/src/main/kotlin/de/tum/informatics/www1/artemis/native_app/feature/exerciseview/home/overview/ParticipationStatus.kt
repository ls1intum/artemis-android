package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus

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
        FlowRow(
            modifier = modifier.padding(8.dp),
            mainAxisSpacing = 8.dp,
            crossAxisAlignment = FlowCrossAxisAlignment.Center
        ) {
            ExerciseActionButtons(
                modifier = Modifier,
                exercise = exercise,
                showResult = showResult,
                actions = actions
            )

            ParticipationStatusUi(
                modifier = Modifier,
                exercise = exercise
            )
        }
    }
}
