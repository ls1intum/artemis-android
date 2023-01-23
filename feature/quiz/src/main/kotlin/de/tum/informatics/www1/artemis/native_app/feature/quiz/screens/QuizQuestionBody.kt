package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens

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
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.ShortAnswerQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.DragAndDropQuizQuestionUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.multiple_choice.MultipleChoiceQuizQuestionUi

@Composable
internal fun QuizQuestionBody(
    modifier: Modifier,
    questionIndex: Int,
    quizQuestionData: QuizQuestionData<*>,
    serverUrl: String,
    authToken: String
) {
    var displayQuestionHint by rememberSaveable { mutableStateOf(false) }
    // Store the hint text of the answer option
    var displayAnswerOptionHint: String? by rememberSaveable { mutableStateOf(null) }

    val onRequestDisplayHint = {
        displayQuestionHint = true
    }

    when (quizQuestionData) {
        is QuizQuestionData.DragAndDropData -> DragAndDropQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            question = quizQuestionData.question,
            data = quizQuestionData,
            serverUrl = serverUrl,
            authToken = authToken,
            onRequestDisplayHint = onRequestDisplayHint
        )

        is QuizQuestionData.MultipleChoiceData -> MultipleChoiceQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            onRequestDisplayAnswerOptionHint = { option ->
                displayAnswerOptionHint = option.hint.orEmpty()
            },
            data = quizQuestionData
        )

        is QuizQuestionData.ShortAnswerData -> ShortAnswerQuizQuestionUi(
            modifier = modifier,
            questionIndex = questionIndex,
            data = quizQuestionData,
            onRequestDisplayHint = onRequestDisplayHint,
        )
    }

    if (displayQuestionHint) {
        HintAlertDialog(
            hint = quizQuestionData.question.hint.orEmpty(),
            onDismissRequest = { displayQuestionHint = false }
        )
    }

    if (displayAnswerOptionHint != null) {
        HintAlertDialog(
            hint = displayAnswerOptionHint.orEmpty(),
            onDismissRequest = { displayAnswerOptionHint = null }
        )
    }
}

@Composable
private fun HintAlertDialog(hint: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_title)) },
        text = { MarkdownText(markdown = hint) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.quiz_participation_question_hint_dialog_positive))
            }
        }
    )
}