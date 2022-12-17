package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation

@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier,
    exercise: Exercise,
    gradedParticipation: StudentParticipation?,
    onClickStartExercise: () -> Unit,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    onViewResult: () -> Unit
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))

        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            exercise = exercise,
            gradedParticipation = gradedParticipation,
            onClickViewResult = onViewResult,
            onClickStartExercise = onClickStartExercise,
            onClickOpenTextExercise = onClickOpenTextExercise
        )
    }
}