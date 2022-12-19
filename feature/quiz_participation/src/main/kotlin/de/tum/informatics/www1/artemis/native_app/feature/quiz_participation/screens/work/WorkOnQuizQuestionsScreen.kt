package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import kotlinx.datetime.Instant

@Composable
internal fun WorkOnQuizQuestionsScreen(
    modifier: Modifier,
    questions: List<QuizQuestion>,
    lastSubmissionTime: Instant?,
    endDate: Instant?
) {
    var selectedQuestionIndex by rememberSaveable(questions.size) { mutableStateOf(0) }

    val currentQuestion = questions.getOrNull(selectedQuestionIndex)

    Column(modifier = modifier) {
        WorkOnQuizHeader(
            modifier = Modifier.fillMaxWidth(),
            questions = questions,
            selectedQuestionIndex = selectedQuestionIndex,
            onChangeSelectionQuestionIndex = { selectedQuestionIndex = it }
        )

        val bodyModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        if (currentQuestion != null) {
            WorkOnQuizBody(
                modifier = bodyModifier,
                currentQuestion = currentQuestion
            )
        } else {
            Box(modifier = bodyModifier)
        }

        WorkOnQuizQuestionFooter(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            lastSubmissionTime = lastSubmissionTime,
            endDate = endDate,
            canNavigateToPreviousQuestion = selectedQuestionIndex > 0,
            canNavigateToNextQuestion = selectedQuestionIndex < questions.size - 1,
            onRequestPreviousQuestion = { selectedQuestionIndex-- },
            onRequestNextQuestion = { selectedQuestionIndex++ }
        )
    }
}

@Composable
private fun WorkOnQuizBody(modifier: Modifier, currentQuestion: QuizQuestion) {
    Box(modifier = modifier) {
        when (currentQuestion) {
            is DragAndDropQuizQuestion -> Text("DRAGNDROP")
            is MultipleChoiceQuizQuestion -> Text("MULTIPLKECHÒIUCE")
            is ShortAnswerQuizQuestion -> Text("SHORT ANSWER")
        }
    }
}