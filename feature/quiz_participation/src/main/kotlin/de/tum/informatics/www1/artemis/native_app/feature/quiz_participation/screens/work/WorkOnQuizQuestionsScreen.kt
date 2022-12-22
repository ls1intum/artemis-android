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
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.MultipleChoiceQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.ShortAnswerQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.draganddrop.DragAndDropQuizQuestionUi
import kotlinx.datetime.Instant

@Composable
internal fun WorkOnQuizQuestionsScreen(
    modifier: Modifier,
    questionsWithData: List<QuizQuestionData<*>>,
    lastSubmissionTime: Instant?,
    endDate: Instant?,
    serverUrl: String,
    authToken: String
) {
    var selectedQuestionIndex by rememberSaveable(questionsWithData.size) { mutableStateOf(0) }

    val currentQuestion = questionsWithData.getOrNull(selectedQuestionIndex)

    Column(modifier = modifier) {
        WorkOnQuizHeader(
            modifier = Modifier.fillMaxWidth(),
            questions = questionsWithData.map { it.question },
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
                quizQuestionData = currentQuestion,
                questionIndex = selectedQuestionIndex,
                serverUrl = serverUrl,
                authToken = authToken
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
            canNavigateToNextQuestion = selectedQuestionIndex < questionsWithData.size - 1,
            onRequestPreviousQuestion = { selectedQuestionIndex-- },
            onRequestNextQuestion = { selectedQuestionIndex++ }
        )
    }
}

@Composable
private fun WorkOnQuizBody(
    modifier: Modifier,
    questionIndex: Int,
    quizQuestionData: QuizQuestionData<*>,
    serverUrl: String,
    authToken: String
) {
    var displayHint by rememberSaveable { mutableStateOf(false) }

    val onRequestDisplayHint = {
        displayHint = true
    }

    when (quizQuestionData) {
        is QuizQuestionData.DragAndDropData -> DragAndDropQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = quizQuestionData.question,
            availableDragItems = quizQuestionData.availableDragItems,
            serverUrl = serverUrl,
            authToken = authToken,
            dropLocationMapping = quizQuestionData.dropLocationMapping,
            onDragItemIntoDropLocation = quizQuestionData.onDragItemIntoDropLocation,
            onClearDropLocation = quizQuestionData.onClearDropLocation,
            onSwapDropLocations = quizQuestionData.onSwapDropLocations,
            onRequestDisplayHint = onRequestDisplayHint
        )

        is QuizQuestionData.MultipleChoiceData -> MultipleChoiceQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = quizQuestionData.question,
            onRequestDisplayHint = onRequestDisplayHint
        )

        is QuizQuestionData.ShortAnswerData -> ShortAnswerQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = quizQuestionData.question,
            onRequestDisplayHint = onRequestDisplayHint,
            solutionTexts = quizQuestionData.solutionTexts,
            onUpdateSolutionText = quizQuestionData.onUpdateSolutionText
        )
    }

    if (displayHint) {
        AlertDialog(
            onDismissRequest = { displayHint = false },
            title = { Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_title)) },
            text = { MarkdownText(markdown = quizQuestionData.question.hint.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { displayHint = false }) {
                    Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_positive))
                }
            }
        )
    }
}

