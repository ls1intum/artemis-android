package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.ServerTimeServiceStub
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import kotlinx.coroutines.flow.emptyFlow

@PlayStoreScreenshots
@Composable
private fun `Quiz - Multiple Choice Question`() {
    val viewModel = QuizParticipationViewModel(
        courseId = 0L,
        exerciseId = 0L,
        quizType = QuizType.Live,
        savedStateHandle = SavedStateHandle(),
        quizExerciseService = object : QuizExerciseService {
            override suspend fun findForStudent(
                exerciseId: Long,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<QuizExercise> = NetworkResponse.Failure(RuntimeException("Stub"))

            override suspend fun join(
                exerciseId: Long,
                password: String,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<QuizExercise.QuizBatch> =
                NetworkResponse.Failure(RuntimeException("Stub"))

        },
        serverConfigurationService = ServerConfigurationServiceStub(),
        accountService = AccountServiceStub(),
        quizParticipationService = object : QuizParticipationService {
            override suspend fun submitForPractice(
                submission: QuizSubmission,
                exerciseId: Long,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<Result> = NetworkResponse.Failure(RuntimeException("Stub"))

            override suspend fun submitForLiveMode(
                submission: QuizSubmission,
                exerciseId: Long,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<Submission> = NetworkResponse.Failure(RuntimeException("Stub"))

        },
        websocketProvider = WebsocketProviderStub(),
        networkStatusProvider = NetworkStatusProviderStub(),
        participationService = object : ParticipationService {
            override val onArtemisContextChanged = emptyFlow<ArtemisContext.LoggedIn>()
            override suspend fun findParticipation(
                exerciseId: Long
            ): NetworkResponse<Participation> = NetworkResponse.Response(
                StudentParticipation.StudentParticipationImpl(
                    exercise = QuizExercise(
                        title = "Quiz: Rocket fuels ⛽",
                        dueDate = null,
                        duration = 60 * 5,
                        maxPoints = 10f,
                        quizBatches = listOf(QuizExercise.QuizBatch(started = true, ended = false)),
                        quizQuestions = listOf(
                            MultipleChoiceQuizQuestion(
                                id = 1L,
                                title = "Solid chemical propellants",
                                text = "What is the primary advantage of using solid chemical propellants in rocket engines?",
                                singleChoice = true,
                                points = 1,
                                answerOptions = listOf(
                                    MultipleChoiceQuizQuestion.AnswerOption(
                                        id = 0L,
                                        text = "High thrust-to-weight ratio \uD83D\uDCC8"
                                    ),
                                    MultipleChoiceQuizQuestion.AnswerOption(
                                        id = 1L,
                                        text = "Easy in-flight thrust adjustments \uD83E\uDDED"
                                    ),
                                    MultipleChoiceQuizQuestion.AnswerOption(
                                        id = 2L,
                                        text = "Lower cost \uD83D\uDCB0"
                                    )
                                )
                            ),
                            DragAndDropQuizQuestion(id = 2L, points = 2),
                            MultipleChoiceQuizQuestion(id = 3L, points = 1),
                            ShortAnswerQuizQuestion(id = 4L, points = 3)
                        )
                    )
                )
            )
        },
        serverTimeService = ServerTimeServiceStub()
    )

    ScreenshotFrame(title = "Participate in your course quizzes") {
        QuizParticipationScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onNavigateToInspectResult = {},
            onNavigateUp = {}
        )
    }
}