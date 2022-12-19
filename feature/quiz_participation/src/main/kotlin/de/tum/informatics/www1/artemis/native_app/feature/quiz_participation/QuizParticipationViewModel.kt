package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quizEnded
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizExerciseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class QuizParticipationViewModel(
    private val courseId: Long,
    private val exerciseId: Long,
    val quizType: QuizType,
    private val quizExerciseService: QuizExerciseService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val participationService: ParticipationService,
    private val websocketProvider: WebsocketProvider
) : ViewModel() {

    private val submissionChannel = "/topic/quizExercise/$exerciseId/submission"
    private val quizExerciseChannel = "/topic/courses/$courseId/quizExercises"

    // TODO: Actually use server clock here.
    private val serverClock: Clock = Clock.System

    private val submissionUpdater: Flow<SubmissionData> = when (quizType) {
        QuizType.LIVE -> {
            websocketProvider.subscribeMessage(
                "user$submissionChannel",
                SubmissionData.serializer()
            ).shareIn(viewModelScope, SharingStarted.Eagerly)
        }

        QuizType.PRACTICE -> emptyFlow()
    }

    private val participationUpdater: Flow<Participation> = when (quizType) {
        QuizType.LIVE -> {
            websocketProvider.subscribeMessage(
                "/user/topic/exercise/$exerciseId/participation",
                Participation.serializer()
            )
                .shareIn(viewModelScope, SharingStarted.Eagerly)
        }

        QuizType.PRACTICE -> emptyFlow()
    }

    private val exerciseUpdater: Flow<QuizExercise> = when (quizType) {
        QuizType.LIVE -> {
            websocketProvider.subscribeMessage(
                quizExerciseChannel,
                QuizExercise.serializer()
            )
        }

        QuizType.PRACTICE -> emptyFlow()
    }

    val isConnected: Flow<Boolean> = websocketProvider.isConnected
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val retryLoadExercise = MutableSharedFlow<Unit>()

    /**
     * In live quizzes, a participation is loaded to get the exercise.
     */
    private val initialParticipationDataState: StateFlow<DataState<Participation>> =
        when (quizType) {
            QuizType.LIVE ->
                retryLoadExercise
                    .onStart { emit(Unit) }
                    .flatMapLatest {
                        transformLatest(
                            serverConfigurationService.serverUrl,
                            accountService.authToken
                        ) { serverUrl, authToken ->
                            emitAll(
                                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                                    participationService.findParticipation(
                                        exerciseId,
                                        serverUrl,
                                        authToken
                                    )
                                }
                            )
                        }
                    }

            QuizType.PRACTICE -> emptyFlow()
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    val initialParticipation: Flow<Participation> = initialParticipationDataState
        .filterSuccess()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val latestParticipation: Flow<Participation> = participationUpdater
        .onStart { emit(initialParticipation.first()) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val quizExerciseDataState: StateFlow<DataState<QuizExercise>> = when (quizType) {
        QuizType.LIVE -> {
            initialParticipationDataState.map { participationDataState ->
                participationDataState.transform { participation ->
                    when (val exercise = participation.exercise) {
                        is QuizExercise -> DataState.Success(exercise)
                        else -> DataState.Failure(RuntimeException("Loaded exercise is not quiz exercise"))
                    }
                }
            }
        }

        QuizType.PRACTICE -> {
            retryLoadExercise
                .onStart { emit(Unit) }
                .flatMapLatest {
                    transformLatest(
                        serverConfigurationService.serverUrl,
                        accountService.authToken
                    ) { serverUrl, authToken ->
                        retryOnInternet(
                            networkStatusProvider.currentNetworkStatus
                        ) {
                            quizExerciseService.findForStudent(
                                exerciseId,
                                serverUrl,
                                authToken
                            )
                        }
                    }
                }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    val quizQuestions: Flow<List<QuizQuestion>> = quizExerciseDataState
        .filterSuccess()
        .map { quizExercise ->
            if (quizExercise.randomizeQuestionOrder == true) {
                quizExercise.quizQuestions.shuffled()
            } else {
                quizExercise.quizQuestions
            }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val batchUpdater: Flow<QuizExercise> = when (quizType) {
        QuizType.LIVE -> {
            quizExerciseDataState.filterSuccess().flatMapLatest { loadedQuizExercise ->
                val batch = loadedQuizExercise.quizBatches.orEmpty().firstOrNull()
                if (batch != null && batch.started != true && batch.id != null) {
                    websocketProvider.subscribeMessage(
                        "$quizExerciseChannel/${batch.id ?: 0}",
                        QuizExercise.serializer()
                    )
                } else emptyFlow()
            }
        }

        QuizType.PRACTICE -> emptyFlow()
    }

    /**
     * Everything depends on this quiz exercise
     */
    private val quizExercise: Flow<QuizExercise> = merge(
        quizExerciseDataState.filterSuccess(),
        exerciseUpdater,
        batchUpdater
    )
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val totalScore: Flow<Int> = quizExercise.map { exercise ->
        exercise.quizQuestions.sumOf { it.points ?: 0 }
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * Emitted to when the user clicks on start batch/join batch
     */
    private val joinBatchFlow = MutableSharedFlow<QuizExercise.QuizBatch>()

    val quizBatch: Flow<QuizExercise.QuizBatch?> = merge(
        quizExercise.map { it.quizBatches.orEmpty().firstOrNull() },
        joinBatchFlow
    )
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val startDate: Flow<Instant> = when (quizType) {
        QuizType.LIVE -> {
            // The start date depends on the quiz batches
            quizBatch.map { batch -> batch?.startTime ?: serverClock.now() }
        }

        QuizType.PRACTICE -> {
            // For practice quizzed, the working time simply starts when the quiz is loaded
            quizExercise.map { Clock.System.now() }
        }
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val endDate: Flow<Instant> = combine(startDate, quizExercise) { startDate, quizExercise ->
        startDate + (quizExercise.duration ?: 0).seconds
    }

    /**
     * The remaining time the student has to complete the quiz.
     */
    val remainingDuration: Flow<Duration> = combine(startDate, endDate) { startDate, endDate ->
        endDate - startDate
    }

    val waitingForQuizStart: Flow<Boolean> = when (quizType) {
        QuizType.LIVE -> {
            combine(quizExercise.flatMapLatest { it.quizEnded }, quizBatch) { quizEnded, batch ->
                if (quizEnded) {
                    false
                } else batch == null || batch.started == false
            }
        }
        // In practice quizzes there is no waiting period.
        QuizType.PRACTICE -> flowOf(false)
    }

    val latestSubmission: StateFlow<QuizSubmission> = flow {
        when (quizType) {
            QuizType.LIVE -> {
                // For live quizzes, the latest submission is the one from the latest participation
                emitAll(
                    latestParticipation
                        .map { latestParticipation ->
                            latestParticipation
                                .results
                                .orEmpty()
                                .firstOrNull()?.submission as? QuizSubmission ?: QuizSubmission()
                        }
                )
            }

            QuizType.PRACTICE -> {}
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, QuizSubmission())

    fun saveSubmission() {
        // TODO: Build submission
        val submission = QuizSubmission()
        viewModelScope.launch {
            val session = websocketProvider.session.first()
            session.convertAndSend(
                headers = StompSendHeaders(destination = submissionChannel),
                submission,
                QuizSubmission.serializer()
            )
        }
    }

    fun retryLoadExercise() {
        viewModelScope.launch {
            retryLoadExercise.emit(Unit)
        }
    }

    fun reconnectWebsocket() {
        viewModelScope.launch {
            websocketProvider.requestTryReconnect()
        }
    }

    fun joinBatch(passcode: String, onFailure: () -> Unit) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            when (val response = quizExerciseService.join(
                exerciseId = exerciseId,
                password = passcode,
                serverUrl = serverUrl,
                authToken = authToken
            )) {
                is NetworkResponse.Failure -> onFailure()
                is NetworkResponse.Response -> {
                    joinBatchFlow.emit(response.data)

                    if (response.data.started == true) {
                        retryLoadExercise.emit(Unit)
                    }
                }
            }

        }
    }

    fun startBatch(onFailure: () -> Unit) {
        joinBatch("", onFailure)
    }

    @Serializable
    private data class SubmissionData(val error: String? = null)
}
