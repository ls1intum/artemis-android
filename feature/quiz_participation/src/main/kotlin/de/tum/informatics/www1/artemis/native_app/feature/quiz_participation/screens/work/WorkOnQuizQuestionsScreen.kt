package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.DragAndDropQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.MultipleChoiceQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.ShortAnswerQuizQuestionUi
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
            .padding(8.dp)
        if (currentQuestion != null) {
            WorkOnQuizBody(
                modifier = bodyModifier,
                currentQuestion = currentQuestion,
                questionIndex = selectedQuestionIndex
            )
        } else {
            Box(modifier = bodyModifier)
        }

        WorkOnQuizQuestionFooter(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
private fun WorkOnQuizBody(modifier: Modifier, questionIndex: Int, currentQuestion: QuizQuestion) {
    var displayHint by rememberSaveable { mutableStateOf(false) }

    val onRequestDisplayHint = {
        displayHint = true
    }

    when (currentQuestion) {
        is DragAndDropQuizQuestion -> DragAndDropQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = currentQuestion,
            onRequestDisplayHint = onRequestDisplayHint
        )

        is MultipleChoiceQuizQuestion -> MultipleChoiceQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = currentQuestion,
            onRequestDisplayHint = onRequestDisplayHint
        )

        is ShortAnswerQuizQuestion -> ShortAnswerQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = currentQuestion,
            onRequestDisplayHint = onRequestDisplayHint
        )
    }

    if (displayHint) {
        AlertDialog(
            onDismissRequest = { displayHint = false },
            title = { Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_title)) },
            text = { MarkdownText(markdown = currentQuestion.hint.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { displayHint = false }) {
                    Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_positive))
                }
            }
        )
    }
}