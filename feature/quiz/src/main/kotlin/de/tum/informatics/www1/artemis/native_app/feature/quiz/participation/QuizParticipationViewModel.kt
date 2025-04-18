package de.tum.informatics.www1.artemis.native_app.feature.quiz.participation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
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
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz.AnswerOptionId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.BaseQuizViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DragAndDropStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DragItemId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DropLocationId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.MultipleChoiceStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.ShortAnswerStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
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
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.parcelize.Parcelize
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import java.util.UUID
import kotlin.Result
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result as SubmissionResult

/**
 * This class persists the answers/solutions of the user in the saved state handle.
 * This implementation does not make any assumptions about the uniqueness of ids across quiz questions.
 */
internal class QuizParticipationViewModel(
    courseId: Long,
    private val exerciseId: Long,
    val quizType: QuizType.WorkableQuizType,
    private val savedStateHandle: SavedStateHandle,
    private val quizExerciseService: QuizExerciseService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val quizParticipationService: QuizParticipationService,
    private val websocketProvider: WebsocketProvider,
    networkStatusProvider: NetworkStatusProvider,
    participationService: ParticipationService,
    serverTimeService: ServerTimeService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseQuizViewModel<QuizParticipationViewModel.DirectShortAnswerStorageData, QuizParticipationViewModel.DirectDragAndDropStorageData, QuizParticipationViewModel.DirectMultipleChoiceStorageData>(
    exerciseId,
    quizType,
    quizExerciseService,
    networkStatusProvider,
    serverConfigurationService,
    accountService,
    participationService,
    coroutineContext
) {

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
    val serverClock: Flow<ClockWithOffset> = serverTimeService.onArtemisContextChanged.flatMapLatest {
        serverTimeService.getServerClock()
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val participationUpdater: Flow<Participation> = when (quizType) {
        QuizType.Live -> {
            websocketProvider.subscribeMessage(
                "/user/topic/exercise/$exerciseId/participation",
                Participation.serializer()
            )
                .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)
        }

        QuizType.Practice -> emptyFlow()
    }

    private val exerciseUpdater: Flow<QuizExercise> = when (quizType) {
        QuizType.Live -> {
            websocketProvider.subscribeMessage(
                quizExerciseChannel,
                QuizExercise.serializer()
            )
        }

        QuizType.Practice -> emptyFlow()
    }

    val isConnected: Flow<Boolean> = websocketProvider.isConnected
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val latestParticipation: Flow<Participation> = participationUpdater
        .onStart { emit(initialParticipation.first()) }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    private val initialSubmission: Flow<QuizSubmission> = latestParticipation
        .map { latestParticipation ->
            latestParticipation
                .results
                .orEmpty()
                .firstOrNull()?.submission as? QuizSubmission ?: QuizSubmission()
        }

    /**
     * Map the question id to the data
     */
    override val shortAnswerData: Flow<Map<Long, DirectShortAnswerStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_SHORT_ANSWER_DATA,
            emptyMap()
        )

    override val dragAndDropData: Flow<Map<Long, DirectDragAndDropStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_DRAG_AND_DROP_DATA,
            emptyMap()
        )

    override val multipleChoiceData: Flow<Map<Long, DirectMultipleChoiceStorageData>> =
        savedStateHandle.getStateFlow(
            TAG_MULTIPLE_CHOICE_DATA,
            emptyMap()
        )

    private val answeredQuestionCount: Flow<Int> = combine(
        quizQuestionsRandomOrder,
        shortAnswerData,
        dragAndDropData,
        multipleChoiceData,
        ::computeAnsweredQuestionCount
    )

    val haveAllQuestionsBeenAnswered: Flow<Boolean> =
        combine(
            answeredQuestionCount,
            quizQuestionsRandomOrder
        ) { answeredQuestionCount, quizQuestions ->
            answeredQuestionCount == quizQuestions.size
        }

    /**
     * Emitted to when the user clicks on start batch/join batch
     */
    private val joinBatchFlow = MutableSharedFlow<QuizExercise.QuizBatch>()

    private val batchUpdater: Flow<QuizExercise> = when (quizType) {
        QuizType.Live -> {
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

        QuizType.Practice -> emptyFlow()
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    /**
     * Everything depends on this quiz exercise
     */
    private val quizExercise: Flow<QuizExercise> = merge(
        quizExerciseDataState.filterSuccess(),
        exerciseUpdater,
        batchUpdater
    )
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    val overallPoints: Flow<Int> = quizExercise.map { exercise ->
        exercise.quizQuestions.sumOf { it.points ?: 0 }
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    val quizBatch: Flow<QuizExercise.QuizBatch?> = merge(
        quizExercise.map { it.quizBatches.orEmpty().firstOrNull() },
        joinBatchFlow
    )
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    /**
     * The start date of the quiz in server time.
     */
    private val startDate: Flow<Instant> = when (quizType) {
        QuizType.Live -> {
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

        QuizType.Practice -> {
            // For practice quizzed, the working time simply starts when the quiz is loaded
            quizExercise.map { Clock.System.now() }
        }
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    val endDate: Flow<Instant> = combine(startDate, quizExercise) { startDate, quizExercise ->
        startDate + (quizExercise.duration ?: 0).seconds
    }

    val waitingForQuizStart: Flow<Boolean> = when (quizType) {
        QuizType.Live -> {
            combine(
                quizExercise.flatMapLatest { it.quizEnded },
                quizBatch
            ) { quizEnded, batch ->
                if (quizEnded) {
                    false
                } else batch == null || batch.started == false
            }
        }
        // In practice quizzes there is no waiting period.
        QuizType.Practice -> flowOf(false)
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
                // If the end date is very close do not wait but send immediately to avoid data loss.
                if (endDate.first() - serverClock.first().now() >= 10.seconds) {
                    // Wait for 1 seconds to avoid sending a submission on every keystroke.
                    delay(1.seconds)
                }
                emit(it)
            }
            .map { (dragAndDropData, multipleChoiceData, shortAnswerData) ->
                val submission = buildAndUploadSubmission(
                    questions = quizQuestionsRandomOrder.first(),
                    isFinalSubmission = false,
                    dragAndDropData = dragAndDropData,
                    multipleChoiceData = multipleChoiceData,
                    shortAnswerData = shortAnswerData,
                    serverClock = serverClock.first()
                )

                val receipt = try {
                    websocketProvider.convertAndSend(
                        headers = StompSendHeaders(
                            destination = submissionChannel,
                            receipt = UUID.randomUUID().toString()
                        ),
                        body = submission,
                        serializer = Submission.serializer()
                    )
                } catch (e: Exception) {
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
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    /**
     * Set when the user has uploaded a submission using the submit button
     */
    private val uploadedSubmission: MutableStateFlow<QuizSubmission?> = MutableStateFlow(null)

    val latestSubmission: StateFlow<QuizSubmission> = flow {
        when (quizType) {
            QuizType.Live -> {
                // For live quizzes, the latest submission is the one from the latest participation
                emitAll(
                    merge(
                        initialSubmission,
                        latestWebsocketSubmission.mapNotNull { it.getOrNull() },
                        uploadedSubmission.filterNotNull()
                    )
                )
            }

            QuizType.Practice -> emitAll(uploadedSubmission.filterNotNull())
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, QuizSubmission())

    val quizEndedStatus: Flow<Boolean> =
        when (quizType) {
            QuizType.Live -> combine(
                quizExercise.flatMapLatest { it.quizEnded },
                quizBatch,
                endDate.flatMapLatest { it.hasPassedFlow() },
                latestParticipation,
                latestSubmission
            ) { quizEnded, batch, endDateHasPassed, latestParticipation, latestSubmission ->
                ((quizEnded) || (batch != null && batch.ended == true))
                        || endDateHasPassed
                        || latestParticipation.initializationState == Participation.InitializationState.FINISHED
                        || latestSubmission.submitted == true
            }

            QuizType.Practice ->
                merge(
                    uploadedSubmission.map { it != null },
                    endDate.flatMapLatest { it.hasPassedFlow() }
                )
        }

    private val resultFromSubmission = MutableSharedFlow<SubmissionResult>()
    val result: StateFlow<SubmissionResult?> =
        merge(
            latestParticipation.map { it.results.orEmpty().firstOrNull() },
            resultFromSubmission
        )
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch(coroutineContext) {
            combine(waitingForQuizStart, quizBatch) { a, b -> a to b }
                .collectLatest { (isWaitingForQuizStart, batch) ->
                    val startTime = batch?.startTime
                    if (isWaitingForQuizStart && startTime != null) {
                        // Wait until the start time has begun. The delay may be restarted multiple times when the server clock changes.
                        serverClock.mapLatest { serverClock ->
                            delay(startTime - serverClock.now() + 1.seconds)
                        }.first()

                        reloadUntilQuizQuestionsAreLoaded()
                    }
                }
        }

        // For batched quizzes, the logic simply swaps from waiting to not waiting
        // Therefore, in these cases we need to trigger a reload
        viewModelScope.launch(coroutineContext) {
            waitingForQuizStart.withPrevious().collectLatest { (previouslyWaiting, nowWaiting) ->
                if (previouslyWaiting == true && !nowWaiting) {

                    reloadUntilQuizQuestionsAreLoaded()
                }
            }
        }

        // Handle disconnects in quiz waiting screen
        viewModelScope.launch(coroutineContext) {
            combine(
                waitingForQuizStart,
                websocketProvider.connectionState
            ) { a, b -> a to b }
                .distinctUntilChanged()
                .collect { (isWaitingForQuizStart, connectionStatus) ->
                    if (isWaitingForQuizStart
                        && connectionStatus is WebsocketProvider.WebsocketConnectionState.WithSession
                        && !connectionStatus.isConnected
                    ) {
                        // we may have missed the exercise start, trigger reload now
                        requestReload.tryEmit(Unit)
                    }
                }
        }

        viewModelScope.launch(coroutineContext) {
            // First wait for the initial submission to load. Then fill the savedStateHandle with it
            val initialSubmission = initialSubmission.first()
            fillSavedStateHandleFromSubmission(initialSubmission)

            hasStoredInitialSubmission.value = true
        }

        if (quizType is QuizType.Practice) {
            viewModelScope.launch(coroutineContext) {
                // Wait for the quiz to end
                quizEndedStatus.first { it }

                // Submit to display results. The ui handles displaying the results automatically
                submit().await()
            }
        }
    }

    private suspend fun reloadUntilQuizQuestionsAreLoaded() {
        while (true) {
            requestReload.tryEmit(Unit)
            delay(1.seconds)

            val questions = quizQuestionsRandomOrder.first()
            val hasEnded = quizEndedStatus.first()
            if (questions.isNotEmpty() || hasEnded) return
        }
    }

    fun submit(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val submission = buildAndUploadSubmission(
                questions = quizQuestionsRandomOrder.first(),
                isFinalSubmission = true,
                shortAnswerData = shortAnswerData.first(),
                dragAndDropData = dragAndDropData.first(),
                multipleChoiceData = multipleChoiceData.first(),
                serverClock = serverClock.first()
            )

            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            when (quizType) {
                QuizType.Live -> quizParticipationService.submitForLiveMode(
                    submission, exerciseId, serverUrl, authToken
                )

                QuizType.Practice -> {
                    val resultResponse = quizParticipationService.submitForPractice(
                        submission, exerciseId, serverUrl, authToken
                    )

                    if (resultResponse is NetworkResponse.Response) {
                        resultFromSubmission.emit(resultResponse.data)
                    }

                    resultResponse
                }
            }
                .onSuccess {
                    uploadedSubmission.value = submission
                }
                .bind { true }
                .or(false)
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
                    dragAndDropData[quizQuestionId] = DirectDragAndDropStorageData(
                        submittedAnswer
                            .mappings
                            .associate {
                                val dragItemId = it.dragItem?.id ?: 0
                                val dropLocationId = it.dropLocation?.id ?: 0
                                dropLocationId to dragItemId
                            }
                    )
                }

                is MultipleChoiceSubmittedAnswer -> {
                    multipleChoiceData[quizQuestionId] =
                        DirectMultipleChoiceStorageData(
                            submittedAnswer
                                .selectedOptions
                                .associate {
                                    it.id to true
                                }
                        )
                }

                is ShortAnswerSubmittedAnswer -> {
                    shortAnswerData[quizQuestionId] =
                        DirectShortAnswerStorageData(
                            submittedAnswer
                                .submittedTexts
                                .associate {
                                    val spotId = it.spot?.spotNr ?: 0
                                    spotId to it.text.orEmpty()
                                }
                        )
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
                    val data = dragAndDropData[question.id]?.value.orEmpty()
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
                    val data = multipleChoiceData[question.id]?.value.orEmpty()
                    val selectedOptions = question.answerOptions.filter { answerOption ->
                        data[answerOption.id] ?: false
                    }

                    MultipleChoiceSubmittedAnswer(
                        selectedOptions = selectedOptions,
                        quizQuestion = question
                    )
                }

                is ShortAnswerQuizQuestion -> {
                    val data = shortAnswerData[question.id]?.value.orEmpty()
                    val submittedTexts: List<ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText> =
                        data.map { (spotNr, text) ->
                            val id = question
                                .spots
                                .firstOrNull { it.spotNr == spotNr }
                                ?.id ?: 0L

                            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                                text = text,
                                spot = ShortAnswerQuizQuestion.ShortAnswerSpot(
                                    id = id,
                                    spotNr = spotNr
                                )
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

    fun reconnectWebsocket() {
        viewModelScope.launch(coroutineContext) {
            websocketProvider.requestTryReconnect()
        }
    }

    fun joinBatch(passcode: String): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            quizExerciseService.join(
                exerciseId = exerciseId,
                password = passcode,
                serverUrl = serverUrl,
                authToken = authToken
            )
                .onSuccess { quizBatch ->
                    joinBatchFlow.emit(quizBatch)

                    if (quizBatch.started == true) {
                        requestReload.emit(Unit)
                    }
                }
                .bind { true }
                .or(false)
        }
    }

    fun startBatch(): Deferred<Boolean> {
        return joinBatch("")
    }

    override fun constructDragAndDropData(
        questionId: Long,
        question: DragAndDropQuizQuestion,
        storageData: DirectDragAndDropStorageData?,
        availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
        dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>
    ): QuizQuestionData.DragAndDropData {
        return QuizQuestionData.DragAndDropData.Editable(
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

    override fun constructMultipleChoiceData(
        questionId: Long,
        question: MultipleChoiceQuizQuestion,
        storageData: DirectMultipleChoiceStorageData?,
        multipleChoiceData: Map<Long, DirectMultipleChoiceStorageData>,
        optionSelectionMapping: Map<AnswerOptionId, Boolean>
    ): QuizQuestionData.MultipleChoiceData {
        return QuizQuestionData.MultipleChoiceData.Editable(
            question = question,
            optionSelectionMapping = optionSelectionMapping,
            onRequestChangeAnswerOptionSelectionState = { optionId, isSelected ->
                // if the mode is single choice, only one option can be selected at a time
                val newOptionSelectionMapping =
                    if (question.singleChoice) mutableMapOf()
                    else optionSelectionMapping.toMutableMap()

                newOptionSelectionMapping[optionId] = isSelected

                val newMultipleChoiceData = multipleChoiceData.toMutableMap()
                newMultipleChoiceData[questionId] =
                    DirectMultipleChoiceStorageData(newOptionSelectionMapping)

                savedStateHandle[TAG_MULTIPLE_CHOICE_DATA] = newMultipleChoiceData
            }
        )
    }

    override fun constructShortAnswerData(
        questionId: Long,
        question: ShortAnswerQuizQuestion,
        storageData: DirectShortAnswerStorageData?,
        shortAnswerData: Map<Long, DirectShortAnswerStorageData>,
        solutionTexts: Map<Int, String>
    ): QuizQuestionData.ShortAnswerData {
        return QuizQuestionData.ShortAnswerData.Editable(
            question = question,
            solutionTexts = solutionTexts,
            onUpdateSolutionText = { spotId, newSolutionText ->
                val newSolutionTexts = solutionTexts.toMutableMap()
                newSolutionTexts[spotId] = newSolutionText

                val newShortAnswerStorageData = shortAnswerData.toMutableMap()
                newShortAnswerStorageData[questionId] =
                    DirectShortAnswerStorageData(newSolutionTexts)

                savedStateHandle[TAG_SHORT_ANSWER_DATA] = newShortAnswerStorageData
            }
        )
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
        val storageData = dragAndDropAnswerData[questionId]?.value.orEmpty()

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
        newAnswerData[questionId] = DirectDragAndDropStorageData(newStorageData)

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
                    ?.value
                    .orEmpty()
                    .isNotEmpty()

                is MultipleChoiceQuizQuestion -> multipleChoiceData[question.id]
                    ?.value
                    .orEmpty()
                    .isNotEmpty()

                is ShortAnswerQuizQuestion -> shortAnswerData[question.id]
                    ?.value
                    .orEmpty()
                    .isNotEmpty()
            }
        }
    }

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

    @Parcelize
    @JvmInline
    value class DirectDragAndDropStorageData(override val value: Map<DropLocationId, DragItemId>) :
        DragAndDropStorageData, Parcelable

    @Parcelize
    @JvmInline
    value class DirectMultipleChoiceStorageData(override val value: Map<AnswerOptionId, Boolean>) :
        MultipleChoiceStorageData, Parcelable

    @Parcelize
    @JvmInline
    value class DirectShortAnswerStorageData(override val value: Map<Int, String>) :
        ShortAnswerStorageData, Parcelable
}
