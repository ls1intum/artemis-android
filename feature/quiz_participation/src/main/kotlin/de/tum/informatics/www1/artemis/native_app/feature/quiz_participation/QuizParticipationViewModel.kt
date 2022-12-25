package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quizEnded
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.websocket.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizParticipationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import okhttp3.internal.wait
import org.hildan.krossbow.stomp.LostReceiptException
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * This class persists the answers/solutions of the user in the saved state handle.
 * This implementation does not make any assumptions about the uniqueness of ids across quiz questions.
 */
internal class QuizParticipationViewModel(
    courseId: Long,
    private val exerciseId: Long,
    val quizType: QuizType,
    private val quizExerciseService: QuizExerciseService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val participationService: ParticipationService,
    private val quizParticipationService: QuizParticipationService,
    private val websocketProvider: WebsocketProvider,
    serverTimeService: ServerTimeService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object {
        private const val TAG_SHORT_ANSWER_DATA = "short_answer_data"
        private const val TAG_DRAG_AND_DROP_DATA = "drag_and_drop_data"
        private const val TAG_MULTIPLE_CHOICE_DATA = "multiple_choice_data"
    }

    private val submissionChannel = "/topic/quizExercise/$exerciseId/submission"
    private val quizExerciseChannel = "/topic/courses/$courseId/quizExercises"

    /**
     * Use server time for best time approximation.
     * The server time may change multiple times, as new clocks may be emitted regularly
     */
    val serverClock: Flow<ClockWithOffset> = serverTimeService
        .serverClock
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

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

    private val retryLoadExercise = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

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

    private val initialParticipation: Flow<Participation> = initialParticipationDataState
        .filterSuccess()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val latestParticipation: Flow<Participation> = participationUpdater
        .onStart { emit(initialParticipation.first()) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val initialSubmission: Flow<QuizSubmission> = latestParticipation
        .map { latestParticipation ->
            latestParticipation
                .results
                .orEmpty()
                .firstOrNull()?.submission as? QuizSubmission ?: QuizSubmission()
        }

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
            transformLatest(
                serverConfigurationService.serverUrl,
                accountService.authToken,
                retryLoadExercise.onStart { emit(Unit) }
            ) { serverUrl, authToken, _ ->
                emitAll(
                    retryOnInternet(
                        networkStatusProvider.currentNetworkStatus
                    ) {
                        quizExerciseService.findForStudent(
                            exerciseId,
                            serverUrl,
                            authToken
                        )
                    }
                )
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    private val quizQuestions: Flow<List<QuizQuestion>> = quizExerciseDataState
        .filterSuccess()
        .map { quizExercise ->
            if (quizExercise.randomizeQuestionOrder == true) {
                quizExercise.quizQuestions.shuffled()
            } else {
                quizExercise.quizQuestions
            }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * Map the question id to the data
     */
    private val shortAnswerData: Flow<Map<Long, ShortAnswerStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_SHORT_ANSWER_DATA,
            emptyMap()
        )

    private val dragAndDropData: Flow<Map<Long, DragAndDropStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_DRAG_AND_DROP_DATA,
            emptyMap()
        )

    private val multipleChoiceData: Flow<Map<Long, MultipleChoiceStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_MULTIPLE_CHOICE_DATA,
            emptyMap()
        )

    /*
     * Construct the question data from the questions and the flows (which are itself coming from the saved state handle)
     */
    val quizQuestionsWithData: Flow<List<QuizQuestionData<*>>> = combine(
        quizQuestions,
        shortAnswerData,
        dragAndDropData,
        multipleChoiceData,
        ::constructQuizQuestionData
    )

    private val answeredQuestionCount: Flow<Int> = combine(
        quizQuestions,
        shortAnswerData,
        dragAndDropData,
        multipleChoiceData,
        ::computeAnsweredQuestionCount
    )

    val haveAllQuestionsBeenAnswered: Flow<Boolean> =
        combine(answeredQuestionCount, quizQuestions) { answeredQuestionCount, quizQuestions ->
            answeredQuestionCount == quizQuestions.size
        }

    /**
     * Emitted to when the user clicks on start batch/join batch
     */
    private val joinBatchFlow = MutableSharedFlow<QuizExercise.QuizBatch>()

    private val batchUpdater: Flow<QuizExercise> = when (quizType) {
        QuizType.LIVE -> {
            val quizExerciseDataStateBatch: Flow<QuizExercise.QuizBatch> = quizExerciseDataState
                .filterSuccess()
                .mapNotNull { it.quizBatches.orEmpty().firstOrNull() }

            merge(
                quizExerciseDataStateBatch,
                joinBatchFlow
            )
                .flatMapLatest { batch ->
                    if (batch.started != true && batch.id != null) {
                        websocketProvider.subscribeMessage(
                            "$quizExerciseChannel/${batch.id ?: 0}",
                            QuizExercise.serializer()
                        )
                    } else emptyFlow()
                }
        }

        QuizType.PRACTICE -> emptyFlow()
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * Everything depends on this quiz exercise
     */
    private val quizExercise: Flow<QuizExercise> = merge(
        quizExerciseDataState.filterSuccess(),
        exerciseUpdater,
        batchUpdater
    )
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val overallPoints: Flow<Int> = quizExercise.map { exercise ->
        exercise.quizQuestions.sumOf { it.points ?: 0 }
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val quizBatch: Flow<QuizExercise.QuizBatch?> = merge(
        quizExercise.map { it.quizBatches.orEmpty().firstOrNull() },
        joinBatchFlow
    )
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    /**
     * The start date of the quiz in server time.
     */
    private val startDate: Flow<Instant> = when (quizType) {
        QuizType.LIVE -> {
            // The start date depends on the quiz batches
            quizBatch.transformLatest { quizBatch ->
                val batchStartTime = quizBatch?.startTime
                if (batchStartTime != null) {
                    emit(batchStartTime)
                } else {
                    val systemTimeStart = Clock.System.now()
                    // The start time always stays the same. But the offset might change
                    emitAll(
                        serverClock.map { clock ->
                            systemTimeStart + clock.delta
                        }
                    )
                }
            }
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

    val hasQuizEnded: Flow<Boolean> =
        combine(
            quizExercise.flatMapLatest { it.quizEnded },
            quizBatch,
            endDate.flatMapLatest { it.hasPassedFlow() }
        ) { quizEnded, batch, endDataHasPassed ->
            (quizType == QuizType.LIVE && ((quizEnded) || (batch != null && batch.ended == true))) || endDataHasPassed
        }

    /**
     * Set to true after the initial submission was loaded and the values have been stored
     */
    private val hasStoredInitialSubmission = MutableStateFlow(false)

    /**
     * Emitted to when buildAndUploadSubmission successfully uploaded a submission through the websocket
     */
    private val onRequestUploadSubmissionToWebsocket =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val latestWebsocketSubmission: Flow<Result<QuizSubmission>> = flow {
        // Wait for the initial storage to avoid uploading an empty submission
        hasStoredInitialSubmission.filter { it }.first()

        var emittedFirst = false
        val submissionFlow = combine(
            dragAndDropData,
            multipleChoiceData,
            shortAnswerData,
            onRequestUploadSubmissionToWebsocket.onStart { emit(Unit) }
        ) { dragAndDropData, multipleChoiceData, shortAnswerData, _ ->
            Triple(dragAndDropData, multipleChoiceData, shortAnswerData)
        }
            .transform {
                if (emittedFirst) {
                    emit(it)
                } else emittedFirst = true
            }
            .transformLatest {
                // Wait for 1 seconds to avoid sending a submission on every keystroke.
                delay(1.seconds)
                emit(it)
            }
            .map { (dragAndDropData, multipleChoiceData, shortAnswerData) ->
                val submission = buildAndUploadSubmission(
                    questions = quizQuestions.first(),
                    isFinalSubmission = false,
                    dragAndDropData = dragAndDropData,
                    multipleChoiceData = multipleChoiceData,
                    shortAnswerData = shortAnswerData,
                    serverClock = serverClock.first()
                )

                val receipt = try {
                    websocketProvider.session.first().convertAndSend(
                        headers = StompSendHeaders(
                            destination = submissionChannel,
                            receipt = UUID.randomUUID().toString()
                        ),
                        body = submission,
                        serializer = Submission.serializer()
                    )
                } catch (exception: LostReceiptException) {
                    null
                }

                if (receipt != null) {
                    Result.success(submission)
                } else {
                    Result.failure(RuntimeException("Could not send through websocket"))
                }
            }

        emitAll(submissionFlow)
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val latestSubmission: StateFlow<QuizSubmission> = flow {
        when (quizType) {
            QuizType.LIVE -> {
                // For live quizzes, the latest submission is the one from the latest participation
                emitAll(
                    merge(
                        initialSubmission,
                        latestWebsocketSubmission.mapNotNull { it.getOrNull() }
                    )
                )
            }

            QuizType.PRACTICE -> {}
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, QuizSubmission())

    init {
        viewModelScope.launch {
            combine(waitingForQuizStart, quizBatch) { a, b -> a to b }
                .collectLatest { (isWaitingForQuizStart, batch) ->
                    val startTime = batch?.startTime
                    if (isWaitingForQuizStart && startTime != null) {
                        // Wait until the start time has begun. The delay may be restarted multiple times when the server clock changes.
                        serverClock.mapLatest { serverClock ->
                            delay(startTime - serverClock.now() + 1.seconds)
                        }.first()

                        retryLoadExercise.emit(Unit)
                    }
                }
        }

        // For batched quizzes, the logic simply swaps from waiting to not waiting
        // Therefore, in these cases we need to trigger a reload
        viewModelScope.launch {
            waitingForQuizStart.withPrevious().collectLatest { (previouslyWaiting, nowWaiting) ->
                if (previouslyWaiting == true && !nowWaiting) {
                    // Use tryEmit, if we are already reloading the quiz, do not trigger it again
                    retryLoadExercise.tryEmit(Unit)
                }
            }
        }

        // Handle disconnects in quiz waiting screen
        viewModelScope.launch {
            combine(
                waitingForQuizStart,
                websocketProvider.isConnected.withPrevious()
            ) { a, b -> a to b }
                .collect { (isWaitingForQuizStart, connectionStatus) ->
                    val (wasConnected, isConnected) = connectionStatus
                    if (isWaitingForQuizStart && wasConnected == false && isConnected) {
                        // we may have missed the exercise start, trigger reload now
                        retryLoadExercise.tryEmit(Unit)
                    }
                }
        }

        viewModelScope.launch {
            // First wait for the initial submission to load. Then fill the savedStateHandle with it
            val initialSubmission = initialSubmission.first()
            fillSavedStateHandleFromSubmission(initialSubmission)

            hasStoredInitialSubmission.value = true
        }
    }

    fun submit(onResponse: (successful: Boolean) -> Unit): Job {
        return viewModelScope.launch {
            val submission = buildAndUploadSubmission(
                questions = quizQuestions.first(),
                isFinalSubmission = true,
                shortAnswerData = shortAnswerData.first(),
                dragAndDropData = dragAndDropData.first(),
                multipleChoiceData = multipleChoiceData.first(),
                serverClock = serverClock.first()
            )

            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            val successful = when (quizType) {
                QuizType.LIVE -> quizParticipationService.submitForLiveMode(
                    submission, exerciseId, serverUrl, authToken
                )

                QuizType.PRACTICE -> quizParticipationService.submitForPractice(
                    submission, exerciseId, serverUrl, authToken
                )
            } is NetworkResponse.Response

            onResponse(successful)
        }
    }

    /**
     * Request to upload a submission of the current state through the websocket
     */
    fun requestSaveSubmissionThroughWebsocket() {
        viewModelScope.launch {
            websocketProvider.requestTryReconnect()
            onRequestUploadSubmissionToWebsocket.tryEmit(Unit)
        }
    }

    /**
     * Load the saved state handle with the values from the given submission
     */
    private fun fillSavedStateHandleFromSubmission(submission: QuizSubmission) {
        val dragAndDropData: MutableMap<Long, DragAndDropStorageData> = mutableMapOf()
        val shortAnswerData: MutableMap<Long, ShortAnswerStorageData> = mutableMapOf()
        val multipleChoiceData: MutableMap<Long, MultipleChoiceStorageData> = mutableMapOf()

        submission.submittedAnswers.forEach { submittedAnswer ->
            val quizQuestionId = submittedAnswer.quizQuestion?.id ?: 0

            when (submittedAnswer) {
                is DragAndDropSubmittedAnswer -> {
                    dragAndDropData[quizQuestionId] = submittedAnswer
                        .mappings
                        .associate {
                            val dragItemId = it.dragItem?.id ?: 0
                            val dropLocationId = it.dropLocation?.id ?: 0
                            dropLocationId to dragItemId
                        }
                }

                is MultipleChoiceSubmittedAnswer -> {
                    multipleChoiceData[quizQuestionId] = submittedAnswer
                        .selectedOptions
                        .associate {
                            it.id to true
                        }
                }

                is ShortAnswerSubmittedAnswer -> {
                    shortAnswerData[quizQuestionId] = submittedAnswer
                        .submittedTexts
                        .associate {
                            val spotId = it.spot?.spotNr ?: 0
                            spotId to it.text.orEmpty()
                        }
                }
            }
        }

        savedStateHandle[TAG_DRAG_AND_DROP_DATA] = dragAndDropData
        savedStateHandle[TAG_SHORT_ANSWER_DATA] = shortAnswerData
        savedStateHandle[TAG_MULTIPLE_CHOICE_DATA] = multipleChoiceData
    }

    /**
     * Constructs a submission from the stored data in the savedStateHandle and uploads it to the server.
     * If isFinalSubmission is set to true, a http request is sent, otherwise the submission is sent
     * through the websocket.
     *
     * @return true if uploading the submission was successful.
     */
    private fun buildAndUploadSubmission(
        questions: List<QuizQuestion>,
        isFinalSubmission: Boolean,
        shortAnswerData: Map<Long, ShortAnswerStorageData>,
        dragAndDropData: Map<Long, DragAndDropStorageData>,
        multipleChoiceData: Map<Long, MultipleChoiceStorageData>,
        serverClock: Clock
    ): QuizSubmission {
        val submittedAnswers: List<SubmittedAnswer> = questions.map { question ->
            when (question) {
                is DragAndDropQuizQuestion -> {
                    val data = dragAndDropData[question.id].orEmpty()
                    val mappedMappingEntries = data
                        .map { (dropLocationId, dragItemId) ->
                            val dropLocation = DragAndDropQuizQuestion.DropLocation(
                                id = dropLocationId
                            )
                            val dragItem = DragAndDropQuizQuestion.DragItem(id = dragItemId)

                            DragAndDropSubmittedAnswer.DragAndDropMapping(
                                dropLocation = dropLocation,
                                dragItem = dragItem
                            )
                        }

                    DragAndDropSubmittedAnswer(
                        mappings = mappedMappingEntries,
                        quizQuestion = question
                    )
                }

                is MultipleChoiceQuizQuestion -> {
                    val data = multipleChoiceData[question.id].orEmpty()
                    val selectedOptions = question.answerOptions.filter { answerOption ->
                        data[answerOption.id] ?: false
                    }

                    MultipleChoiceSubmittedAnswer(
                        selectedOptions = selectedOptions,
                        quizQuestion = question
                    )
                }

                is ShortAnswerQuizQuestion -> {
                    val data = shortAnswerData[question.id].orEmpty()
                    val submittedTexts: List<ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText> =
                        data.map { (spotId, text) ->
                            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                                text = text,
                                spot = ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr = spotId)
                            )
                        }

                    ShortAnswerSubmittedAnswer(
                        quizQuestion = question,
                        submittedTexts = submittedTexts
                    )
                }
            }
        }

        return QuizSubmission(
            submissionDate = serverClock.now(),
            submitted = isFinalSubmission,
            submittedAnswers = submittedAnswers,
            results = emptyList()
        )
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

    private fun constructQuizQuestionData(
        questions: List<QuizQuestion>,
        shortAnswerData: Map<Long, ShortAnswerStorageData>,
        dragAndDropAnswerData: Map<Long, DragAndDropStorageData>,
        multipleChoiceData: Map<Long, MultipleChoiceStorageData>
    ): List<QuizQuestionData<*>> {
        return questions.map { question ->
            val questionId = question.id ?: 0

            when (question) {
                is DragAndDropQuizQuestion -> {
                    val storageData: DragAndDropStorageData =
                        dragAndDropAnswerData[questionId].orEmpty()

                    // Construct the mapping from the saved key value id pairs
                    val dropLocationMapping = storageData
                        .mapNotNull { (locationId, dragItemId) ->
                            val dropLocation = question.dropLocationById[locationId]
                            val dragItem = question.dragItemById[dragItemId]

                            if (dropLocation != null && dragItem != null) {
                                dropLocation to dragItem
                            } else null // This should not happen.
                        }.toMap()

                    // The available items are the ones not yet placed in a drop location
                    val availableDragItems = question.dragItems.filter {
                        it !in dropLocationMapping.values
                    }

                    QuizQuestionData.DragAndDropData(
                        question = question,
                        availableDragItems = availableDragItems,
                        dropLocationMapping = dropLocationMapping,
                        onDragItemIntoDropLocation = { itemId, dropId ->
                            updateDragAndDropDropLocation(
                                questionId = questionId,
                                action = DragAndDropAction.Put(dropId, itemId)
                            )
                        },
                        onClearDropLocation = { dropId ->
                            updateDragAndDropDropLocation(
                                questionId = questionId,
                                action = DragAndDropAction.Clear(dropId)
                            )
                        },
                        onSwapDropLocations = { oldDropLocationId, newDropLocationId ->
                            updateDragAndDropDropLocation(
                                questionId = questionId,
                                action = DragAndDropAction.Swap(
                                    oldDropLocationId,
                                    newDropLocationId
                                )
                            )
                        }
                    )
                }

                is MultipleChoiceQuizQuestion -> {
                    val optionSelectionMapping = multipleChoiceData[questionId].orEmpty()

                    QuizQuestionData.MultipleChoiceData(
                        question = question,
                        optionSelectionMapping = optionSelectionMapping,
                        onRequestChangeAnswerOptionSelectionState = { optionId, isSelected ->
                            // if the mode is single choice, only one option can be selected at a time
                            val newOptionSelectionMapping =
                                if (question.singleChoice) mutableMapOf()
                                else optionSelectionMapping.toMutableMap()

                            newOptionSelectionMapping[optionId] = isSelected

                            val newMultipleChoiceData = multipleChoiceData.toMutableMap()
                            newMultipleChoiceData[questionId] = newOptionSelectionMapping

                            savedStateHandle[TAG_MULTIPLE_CHOICE_DATA] = newMultipleChoiceData
                        }
                    )
                }

                is ShortAnswerQuizQuestion -> {
                    val solutionTexts: Map<Int, String> = shortAnswerData[questionId].orEmpty()

                    QuizQuestionData.ShortAnswerData(
                        question = question,
                        solutionTexts = solutionTexts,
                        onUpdateSolutionText = { spotId, newSolutionText ->
                            val newSolutionTexts = solutionTexts.toMutableMap()
                            newSolutionTexts[spotId] = newSolutionText

                            val newShortAnswerStorageData = shortAnswerData.toMutableMap()
                            newShortAnswerStorageData[questionId] = newSolutionTexts

                            savedStateHandle[TAG_SHORT_ANSWER_DATA] = newShortAnswerStorageData
                        }
                    )
                }
            }
        }
    }

    /**
     * Updates the drag and drop storage in the saved state handle.
     */
    private fun updateDragAndDropDropLocation(
        questionId: Long,
        action: DragAndDropAction
    ) {
        val dragAndDropAnswerData: Map<Long, DragAndDropStorageData> =
            savedStateHandle.get<Map<Long, DragAndDropStorageData>>(TAG_DRAG_AND_DROP_DATA)
                .orEmpty()
        val storageData = dragAndDropAnswerData[questionId].orEmpty()

        val newStorageData = storageData.toMutableMap()

        when (action) {
            is DragAndDropAction.Put -> {
                newStorageData[action.dropLocationId] = action.dragItemId
            }

            is DragAndDropAction.Clear -> {
                newStorageData.remove(action.dropLocationId)
            }

            is DragAndDropAction.Swap -> {
                // there must be an item in previous.
                val inPrevious = newStorageData[action.previousDropLocationId] ?: return

                val inNew = newStorageData[action.newDropLocationId]
                if (inNew != null) {
                    newStorageData[action.previousDropLocationId] = inNew
                } else {
                    newStorageData.remove(action.previousDropLocationId)
                }

                newStorageData[action.newDropLocationId] = inPrevious
            }
        }

        val newAnswerData = dragAndDropAnswerData.toMutableMap()
        newAnswerData[questionId] = newStorageData

        savedStateHandle[TAG_DRAG_AND_DROP_DATA] = newAnswerData
    }

    /**
     * Tests for each question if the student has interacted with.
     */
    private fun computeAnsweredQuestionCount(
        questions: List<QuizQuestion>,
        shortAnswerData: Map<Long, ShortAnswerStorageData>,
        dragAndDropAnswerData: Map<Long, DragAndDropStorageData>,
        multipleChoiceData: Map<Long, MultipleChoiceStorageData>
    ): Int {
        return questions.count { question ->
            when (question) {
                is DragAndDropQuizQuestion -> dragAndDropAnswerData[question.id]
                    .orEmpty()
                    .isNotEmpty()

                is MultipleChoiceQuizQuestion -> multipleChoiceData[question.id]
                    .orEmpty()
                    .isNotEmpty()

                is ShortAnswerQuizQuestion -> shortAnswerData[question.id]
                    .orEmpty()
                    .isNotEmpty()
            }
        }
    }

    @Serializable
    private data class SubmissionData(val error: String? = null)

    private sealed interface DragAndDropAction {
        /***
         * Move from the available drop items to a drop location
         */
        data class Put(val dropLocationId: DropLocationId, val dragItemId: DragItemId) :
            DragAndDropAction

        /**
         * Move from one drop location to another, swapping the contents.
         * If one location does not hold a drag item, the null value is swapped.
         */
        data class Swap(
            val previousDropLocationId: DropLocationId,
            val newDropLocationId: DropLocationId
        ) : DragAndDropAction

        /**
         * Clear the drop location, returning the present drag item to the available items.
         */
        data class Clear(
            val dropLocationId: DropLocationId
        ) : DragAndDropAction
    }
}

private typealias ShortAnswerStorageData = Map<Int, String>
private typealias DropLocationId = Long
private typealias DragItemId = Long
private typealias DragAndDropStorageData = Map<DropLocationId, DragItemId>
private typealias AnswerOptionId = Long
private typealias MultipleChoiceStorageData = Map<AnswerOptionId, Boolean>