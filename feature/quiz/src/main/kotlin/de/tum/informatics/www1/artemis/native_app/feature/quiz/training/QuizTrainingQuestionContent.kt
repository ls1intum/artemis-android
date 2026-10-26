package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizQuestionBody
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.LocalQuizReviewPrecision

/** Training adds accessible input alternatives while preserving the shared native question body. */
@Composable
internal fun TrainingQuestionContent(
    state: TrainingState,
    session: QuizTrainingSession,
    precision: Int,
) {
    val resources = LocalResources.current
    CompositionLocalProvider(LocalQuizReviewPrecision provides precision) {
        QuizQuestionBody(
            Modifier.fillMaxWidth(),
            state.answered - (if (state.evaluation != null) 1 else 0),
            state.evaluation?.let(::trainingResultData) ?: trainingEditableData(state, session),
            shortAnswerBlankLabel = { number ->
                resources.getString(R.string.training_blank_label, number)
            },
        )
    }
    val question = state.current?.quizQuestionWithSolutionDTO as? DragAndDropQuizQuestion
    if (question != null && state.stage == TrainingStage.ANSWERING) {
        TrainingDragAndDropChoices(question, state, session)
    }
}
