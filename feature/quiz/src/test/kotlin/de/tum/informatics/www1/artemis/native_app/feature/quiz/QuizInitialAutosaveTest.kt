package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.offsetBy
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizReviewAttempt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizInitialAutosaveTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun `first editable answer is retained and autosaved without another edit`() = runTest(dispatcher) {
        val fixture = Fixture(this)
        val model = fixture.model()
        // A student can answer as soon as the question becomes editable, before other queued collectors run.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            model.quizQuestionsWithData.filterSuccess().first().single().mc()
                .onRequestChangeAnswerOptionSelectionState(102, true)
        }
        runCurrent()
        assertEquals(listOf(listOf(102L)), fixture.saves.map(::selected))
        assertEquals(true, model.quizQuestionsWithData.value.orThrow().single().mc().optionSelectionMapping[102])
    }

    @Test fun `first editable question restores server answers before accepting another selection`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        val model = fixture.model()
        var firstSelection: Map<Long, Boolean>? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val question = model.quizQuestionsWithData.filterSuccess().first().single().mc()
            firstSelection = question.optionSelectionMapping
            question.onRequestChangeAnswerOptionSelectionState(102, true)
        }
        runCurrent()
        assertEquals(mapOf(101L to true), firstSelection)
        assertEquals(listOf(listOf(101L, 102L)), fixture.saves.map(::selected))
    }

    @Test fun `untouched restored answers are not autosaved`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        val model = fixture.model()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            model.quizQuestionsWithData.collect {}
        }
        runCurrent()
        advanceTimeBy(31.seconds)
        runCurrent()
        assertEquals(mapOf(101L to true), model.quizQuestionsWithData.value.orThrow().single().mc().optionSelectionMapping)
        assertTrue(fixture.saves.isEmpty())
    }

    @Test fun `returning to restored answers is saved after the autosave interval`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        val model = fixture.model()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.quizQuestionsWithData.collect {} }
        runCurrent()
        model.quizQuestionsWithData.value.orThrow().single().mc().onRequestChangeAnswerOptionSelectionState(101, false)
        runCurrent()
        assertEquals(listOf(emptyList<Long>()), fixture.saves.map(::selected))
        model.quizQuestionsWithData.value.orThrow().single().mc().onRequestChangeAnswerOptionSelectionState(101, true)
        runCurrent()
        assertEquals(1, fixture.saves.size)
        advanceTimeBy(30.seconds)
        runCurrent()
        assertEquals(listOf(emptyList<Long>(), listOf(101L)), fixture.saves.map(::selected))
    }

    @Test fun `explicit retry preserves first answer and bypasses the autosave interval`() = runTest(dispatcher) {
        val fixture = Fixture(this)
        fixture.saveFailure = true
        val model = fixture.model()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            model.quizQuestionsWithData.filterSuccess().first().single().mc()
                .onRequestChangeAnswerOptionSelectionState(102, true)
        }
        runCurrent()
        assertEquals(listOf(listOf(102L)), fixture.saves.map(::selected))
        assertTrue(model.latestSavedSubmission.first().isFailure)
        fixture.saveFailure = false
        model.requestSaveSubmission()
        runCurrent()
        assertEquals(listOf(listOf(102L), listOf(102L)), fixture.saves.map(::selected))
        assertTrue(model.latestSavedSubmission.first().isSuccess)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test fun `practice remains editable and submits its first answer without live autosave`() = runTest(dispatcher) {
        val fixture = Fixture(this)
        val model = fixture.model(QuizType.Practice)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            model.quizQuestionsWithData.filterSuccess().first().single().mc()
                .onRequestChangeAnswerOptionSelectionState(102, true)
        }
        runCurrent()
        assertEquals(true, model.quizQuestionsWithData.value.orThrow().single().mc().optionSelectionMapping[102])
        assertTrue(fixture.saves.isEmpty())
        val submitted = model.submit()
        runCurrent()
        assertTrue(submitted.await())
        assertEquals(listOf(listOf(102L)), fixture.submits.map(::selected))
    }

    @Test fun `submit before question hydration sends the restored server answers`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        val model = fixture.model()
        val submitted = model.submit()
        runCurrent()
        assertTrue(submitted.await())
        assertEquals(listOf(listOf(101L)), fixture.submits.map(::selected))
        assertTrue(fixture.saves.isEmpty())
    }

    @Test fun `submit while restoration is queued sends the restored answers`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        val queue = QueuedDispatcher()
        val model = fixture.model(context = backgroundScope.coroutineContext + queue)
        // The exercise enables Submit before its independently scheduled answer restoration completes.
        var steps = 0
        while (model.quizExerciseDataState.value !is de.tum.informatics.www1.artemis.native_app.core.data.DataState.Success) {
            check(steps++ < 1000 && queue.runFirst()) { "Exercise did not load" }
        }
        val submitted = model.submit()
        // Let the newly requested submit and its dependencies run ahead of older queued work.
        while (!submitted.isCompleted) {
            check(steps++ < 2000 && queue.runLast()) { "Submission did not complete" }
        }
        assertTrue(submitted.await())
        assertEquals(listOf(listOf(101L)), fixture.submits.map(::selected))
    }

    @Test fun `failed final submission remains retryable and successful submission is sent only once`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        fixture.submitFailure = true
        val model = fixture.model()
        val failed = model.submit()
        runCurrent()
        assertFalse(failed.await())
        fixture.submitFailure = false
        val retried = model.submit()
        runCurrent()
        assertTrue(retried.await())
        val alreadySubmitted = model.submit()
        runCurrent()
        assertTrue(alreadySubmitted.await())
        assertEquals(listOf(listOf(101L), listOf(101L)), fixture.submits.map(::selected))
    }

    @Test fun `submit waits for autosave then reads the latest answer and prevents later saves`() = runTest(dispatcher) {
        val fixture = Fixture(this, restoredOptions = listOf(101))
        fixture.blockedSave = CompletableDeferred()
        val model = fixture.model()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.quizQuestionsWithData.collect {} }
        runCurrent()
        model.quizQuestionsWithData.value.orThrow().single().mc().onRequestChangeAnswerOptionSelectionState(101, false)
        runCurrent()
        assertEquals(listOf(emptyList<Long>()), fixture.saves.map(::selected))
        model.quizQuestionsWithData.value.orThrow().single().mc().onRequestChangeAnswerOptionSelectionState(101, true)
        val submitted = model.submit()
        runCurrent()
        assertTrue(fixture.submits.isEmpty())
        assertFalse(submitted.isCompleted)
        fixture.blockedSave!!.complete(Unit)
        runCurrent()
        assertTrue(submitted.await())
        assertEquals(listOf(listOf(101L)), fixture.submits.map(::selected))
        advanceTimeBy(31.seconds)
        runCurrent()
        assertEquals(1, fixture.saves.size)
    }

    private fun QuizQuestionData<*>.mc() = this as QuizQuestionData.MultipleChoiceData.Editable
    private fun selected(submission: QuizSubmission): List<Long> = submission.submittedAnswers
        .filterIsInstance<MultipleChoiceSubmittedAnswer>().single().selectedOptions.map { it.id }

    private class QueuedDispatcher : CoroutineDispatcher() {
        private val tasks = ArrayDeque<Runnable>()
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.addLast(block) }
        fun runFirst(): Boolean = tasks.removeFirstOrNull()?.let { it.run(); true } ?: false
        fun runLast(): Boolean = tasks.removeLastOrNull()?.let { it.run(); true } ?: false
    }

    private class Fixture(private val scope: TestScope, restoredOptions: List<Long> = emptyList()) {
        private val now = Clock.System.now()
        private val session = ArtemisContextImpl.LoggedIn("https://artemis.example", "token", "student")
        private val question = MultipleChoiceQuizQuestion(
            id = 7, singleChoice = false,
            answerOptions = listOf(
                MultipleChoiceQuizQuestion.AnswerOption(id = 101),
                MultipleChoiceQuizQuestion.AnswerOption(id = 102),
            ),
        )
        private val quiz = QuizExercise(
            id = 42, duration = 600, quizQuestions = listOf(question),
            quizBatches = listOf(QuizExercise.QuizBatch(id = 1, started = true, startTime = now)),
        )
        private val participation = StudentParticipation.StudentParticipationImpl(
            id = 9, initializationState = Participation.InitializationState.INITIALIZED,
            exercise = quiz,
            submissions = listOf(QuizSubmission(id = 3, submitted = false, submittedAnswers = listOf(
                MultipleChoiceSubmittedAnswer(
                    quizQuestion = question,
                    selectedOptions = question.answerOptions.filter { it.id in restoredOptions },
                ),
            ))),
        )
        val saves = mutableListOf<QuizSubmission>()
        var saveFailure = false
        var submitFailure = false
        var blockedSave: CompletableDeferred<Unit>? = null
        val submits = mutableListOf<QuizSubmission>()
        private val uploads = object : QuizParticipationService {
            override suspend fun saveForLiveMode(submission: QuizSubmission, exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Submission> {
                saves += submission
                blockedSave?.await()
                return if (saveFailure) NetworkResponse.Failure(IOException("offline")) else NetworkResponse.Response(submission)
            }
            override suspend fun submitForLiveMode(submission: QuizSubmission, exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Submission> {
                submits += submission
                return if (submitFailure) NetworkResponse.Failure(IOException("offline")) else NetworkResponse.Response(submission)
            }
            override suspend fun submitForPractice(submission: QuizSubmission, exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Result> {
                submits += submission
                return if (submitFailure) NetworkResponse.Failure(IOException("offline")) else NetworkResponse.Response(Result(submission = submission))
            }
        }
        private val participationService = object : ParticipationService {
            override val onArtemisContextChanged = flowOf(session)
            override suspend fun findParticipation(exerciseId: Long) = NetworkResponse.Response<Participation>(participation)
        }
        private val exerciseService = object : QuizExerciseService {
            override suspend fun findReviewAttempts(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<List<QuizReviewAttempt>> = error("not reviewing attempts")
            override suspend fun findResultParticipation(
                exerciseId: Long, serverUrl: String, authToken: String, participationId: Long?, submissionId: Int?,
            ): NetworkResponse<Participation> = error("not reviewing results")
            override suspend fun findForStudent(exerciseId: Long, serverUrl: String, authToken: String) = NetworkResponse.Response(quiz)
            override suspend fun join(exerciseId: Long, password: String, serverUrl: String, authToken: String): NetworkResponse<QuizExercise.QuizBatch> = error("not joining")
        }
        private val server = object : ServerConfigurationService {
            override val serverUrl = flowOf(session.serverUrl)
            override suspend fun updateServerUrl(serverUrl: String) = Unit
        }
        private val account = object : AccountService {
            override val authenticationData = flowOf(AccountService.AuthenticationData.LoggedIn(session.authToken, session.loginName))
            override suspend fun storeAccessToken(jwt: String, rememberMe: Boolean) = Unit
            override suspend fun logout() = Unit
        }
        private val network = object : NetworkStatusProvider {
            override val currentNetworkStatus = flowOf(NetworkStatusProvider.NetworkStatus.Internet)
        }
        private val time = object : ServerTimeService {
            override val onArtemisContextChanged = flowOf(session)
            override fun getServerClock() = flowOf(object : Clock {
                override fun now() = now + scope.testScheduler.currentTime.milliseconds
            }.offsetBy(Duration.ZERO))
        }
        private val websocket = object : WebsocketProvider {
            override val connectionState = flowOf(WebsocketProvider.WebsocketConnectionState.WithSession(true))
            override val isConnected = flowOf(true)
            override fun <T : Any> subscribe(topic: String, deserializer: DeserializationStrategy<T>): Flow<WebsocketProvider.WebsocketData<T>> = emptyFlow()
            override fun <T : Any> subscribeMessage(topic: String, deserializer: DeserializationStrategy<T>): Flow<T> = emptyFlow()
            override suspend fun <T> convertAndSend(headers: StompSendHeaders, body: T, serializer: SerializationStrategy<T>): StompReceipt? = null
            override fun requestTryReconnect() = Unit
        }
        fun model(
            type: QuizType.WorkableQuizType = QuizType.Live,
            context: CoroutineContext = scope.backgroundScope.coroutineContext,
        ) = QuizParticipationViewModel(
            courseId = 1, exerciseId = quiz.id, quizType = type, savedStateHandle = SavedStateHandle(),
            quizExerciseService = exerciseService, serverConfigurationService = server, accountService = account,
            quizParticipationService = uploads, websocketProvider = websocket, networkStatusProvider = network,
            participationService = participationService, serverTimeService = time,
            coroutineContext = context,
        )
    }
}
