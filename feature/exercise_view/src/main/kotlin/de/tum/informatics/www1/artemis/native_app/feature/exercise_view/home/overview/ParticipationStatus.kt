package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.koin.androidx.compose.get

/**
 * Display the default participation status ui in combination with enhancing options.
 * These additional options are, e.g, a button to inspect the result.
 * @param onClickViewResult called when the user wants to view their latest result.
 */
@Composable
internal fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise,
    gradedParticipation: Participation?,
    showResult: Boolean = true,
    onClickStartExercise: () -> Unit,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    onClickViewResult: () -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit
) {
    ProvideDefaultExerciseTemplateStatus(exercise) {
        FlowRow(
            modifier = modifier,
            mainAxisSpacing = 8.dp,
            crossAxisAlignment = FlowCrossAxisAlignment.Center
        ) {
            ExerciseActionButtons(
                modifier = Modifier,
                exercise = exercise,
                gradedParticipation = gradedParticipation,
                onClickStartExercise = onClickStartExercise,
                onClickPracticeQuiz = onClickPracticeQuiz,
                onClickOpenQuiz = onClickOpenQuiz,
                onClickStartQuiz = onClickStartQuiz,
                onClickOpenTextExercise = onClickOpenTextExercise,
                showResult = showResult,
                onClickViewResult = onClickViewResult
            )

            ParticipationStatusUi(
                modifier = Modifier,
                exercise = exercise
            )
        }
    }
}
