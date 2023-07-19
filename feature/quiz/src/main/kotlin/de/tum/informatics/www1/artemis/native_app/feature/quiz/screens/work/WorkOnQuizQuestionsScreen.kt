package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizQuestionBody
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal const val TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN = "TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN"

@Composable
internal fun WorkOnQuizQuestionsScreen(
    modifier: Modifier,
    quizType: QuizType.WorkableQuizType,
    questionsWithData: List<QuizQuestionData<*>>,
    lastSubmissionTime: Instant?,
    endDate: Instant?,
    isConnected: Boolean,
    overallPoints: Int,
    latestWebsocketSubmission: Result<QuizSubmission>?,
    clock: Clock,
    serverUrl: String,
    authToken: String,
    onRequestRetrySave: () -> Unit
) {
    var selectedQuestionIndex by rememberSaveable(questionsWithData.size) { mutableStateOf(0) }

    val currentQuestion = questionsWithData.getOrNull(selectedQuestionIndex)

    Column(modifier = modifier.testTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN)) {
        WorkOnQuizHeader(
            modifier = Modifier.fillMaxWidth(),
            questions = questionsWithData.map { it.question },
            selectedQuestionIndex = selectedQuestionIndex,
            overallPoints = overallPoints
        ) { selectedQuestionIndex = it }

        val bodyModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
        if (currentQuestion != null) {
            QuizQuestionBody(
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
            quizType = quizType,
            lastSubmissionTime = lastSubmissionTime,
            latestWebsocketSubmission = latestWebsocketSubmission,
            isConnected = isConnected,
            endDate = endDate,
            clock = clock,
            canNavigateToPreviousQuestion = selectedQuestionIndex > 0,
            canNavigateToNextQuestion = selectedQuestionIndex < questionsWithData.size - 1,
            onRequestPreviousQuestion = { selectedQuestionIndex-- },
            onRequestNextQuestion = { selectedQuestionIndex++ },
            onRequestRetrySave = onRequestRetrySave
        )
    }
}
