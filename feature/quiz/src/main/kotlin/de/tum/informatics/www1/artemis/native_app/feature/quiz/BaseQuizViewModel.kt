package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
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
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal abstract class BaseQuizViewModel<
        ShortAnswerStorageDataT : ShortAnswerStorageData,
        DragAndDropStorageDataT : DragAndDropStorageData,
        MultipleChoiceStorageDataT : MultipleChoiceStorageData>(
    private val exerciseId: Long,
    quizType: QuizType,
    private val quizExerciseService: QuizExerciseService,
    private val networkStatusProvider: NetworkStatusProvider,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val participationService: ParticipationService,
) : ViewModel() {

    protected val retryLoadExercise = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * In live quizzes, a participation is loaded to get the exercise.
     */
    protected val initialParticipationDataState: StateFlow<DataState<Participation>> =
        when (quizType) {
            QuizType.Live, QuizType.ViewResults ->
                flatMapLatest(
                    serverConfigurationService.serverUrl,
                    accountService.authToken,
                    retryLoadExercise.onStart { emit(Unit) }
                ) { serverUrl, authToken, _ ->
                    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                        participationService.findParticipation(
                            exerciseId,
                            serverUrl,
                            authToken
                        )
                    }
                }

            QuizType.Practice, is QuizType.PracticeResults -> emptyFlow()
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    protected val initialParticipation: Flow<Participation> = initialParticipationDataState
        .filterSuccess()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val quizExerciseDataState: StateFlow<DataState<QuizExercise>> = when (quizType) {
        QuizType.Live, QuizType.ViewResults -> {
            initialParticipationDataState.map { participationDataState ->
                participationDataState.transform { participation ->
                    when (val exercise = participation.exercise) {
                        is QuizExercise -> DataState.Success(exercise)
                        else -> DataState.Failure(RuntimeException("Loaded exercise is not quiz exercise"))
                    }
                }
            }
        }

        QuizType.Practice -> {
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

        is QuizType.PracticeResults -> flowOf(DataState.Success(quizType.quizExercise))
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * The maximum of points achievable
     */
    val maxPoints: StateFlow<DataState<Int>> = quizExerciseDataState
        .map { quizExerciseDataState ->
            quizExerciseDataState.bind { quizExercise ->
                quizExercise.quizQuestions.sumOf { it.points ?: 0 }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    protected val quizQuestionsRandomOrder: Flow<List<QuizQuestion>> = quizExerciseDataState
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
    protected abstract val shortAnswerData: Flow<Map<Long, ShortAnswerStorageDataT>>

    protected abstract val dragAndDropData: Flow<Map<Long, DragAndDropStorageDataT>>

    protected abstract val multipleChoiceData: Flow<Map<Long, MultipleChoiceStorageDataT>>

    /*
     * Construct the question data from the questions and the flows (which are itself coming from the saved state handle)
     */
    val quizQuestionsWithData: StateFlow<DataState<List<QuizQuestionData<*>>>> = flow {
        // Wrap in flow to avoid compiler warning as properties are abstract
        emitAll(
            combine(
                quizExerciseDataState.map { quizExercise -> quizExercise.bind { it.quizQuestions } },
                shortAnswerData,
                dragAndDropData,
                multipleChoiceData
            ) { questionDataState, shortAnswerData, dragAndDropData, multipleChoiceData ->
                questionDataState.bind { questions ->
                    constructQuizQuestionData(
                        questions = questions,
                        shortAnswerData = shortAnswerData,
                        dragAndDropAnswerData = dragAndDropData,
                        multipleChoiceData = multipleChoiceData
                    )
                }
            }
        )
    }
        .stateIn(viewModelScope, SharingStarted.Lazily)

    fun retryLoadExercise() {
        viewModelScope.launch {
            retryLoadExercise.emit(Unit)
        }
    }

    private fun constructQuizQuestionData(
        questions: List<QuizQuestion>,
        shortAnswerData: Map<Long, ShortAnswerStorageDataT>,
        dragAndDropAnswerData: Map<Long, DragAndDropStorageDataT>,
        multipleChoiceData: Map<Long, MultipleChoiceStorageDataT>
    ): List<QuizQuestionData<*>> {
        return questions.map { question ->
            val questionId = question.id

            when (question) {
                is DragAndDropQuizQuestion -> {
                    val storageData: DragAndDropStorageDataT? = dragAndDropAnswerData[questionId]

                    val storageDataValue = storageData?.value.orEmpty()

                    // Construct the mapping from the saved key value id pairs
                    val dropLocationMapping = storageDataValue
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

                    constructDragAndDropData(
                        questionId = questionId,
                        question = question,
                        availableDragItems = availableDragItems,
                        dropLocationMapping = dropLocationMapping,
                        storageData = storageData
                    )
                }

                is MultipleChoiceQuizQuestion -> {
                    val storageData: MultipleChoiceStorageDataT? = multipleChoiceData[questionId]
                    val optionSelectionMapping: Map<AnswerOptionId, Boolean> =
                        storageData?.value.orEmpty()

                    constructMultipleChoiceData(
                        questionId = questionId,
                        question = question,
                        storageData = storageData,
                        multipleChoiceData = multipleChoiceData,
                        optionSelectionMapping = optionSelectionMapping
                    )
                }

                is ShortAnswerQuizQuestion -> {
                    val storageData: ShortAnswerStorageDataT? = shortAnswerData[questionId]
                    val solutionTexts: Map<Int, String> = storageData?.value.orEmpty()

                    constructShortAnswerData(
                        questionId = questionId,
                        question = question,
                        storageData = storageData,
                        shortAnswerData = shortAnswerData,
                        solutionTexts = solutionTexts
                    )
                }
            }
        }
    }

    abstract fun constructDragAndDropData(
        questionId: Long,
        question: DragAndDropQuizQuestion,
        storageData: DragAndDropStorageDataT?,
        availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
        dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>
    ): QuizQuestionData.DragAndDropData

    abstract fun constructMultipleChoiceData(
        questionId: Long,
        question: MultipleChoiceQuizQuestion,
        storageData: MultipleChoiceStorageDataT?,
        multipleChoiceData: Map<Long, MultipleChoiceStorageDataT>,
        optionSelectionMapping: Map<AnswerOptionId, Boolean>
    ): QuizQuestionData.MultipleChoiceData

    abstract fun constructShortAnswerData(
        questionId: Long,
        question: ShortAnswerQuizQuestion,
        storageData: ShortAnswerStorageDataT?,
        shortAnswerData: Map<Long, ShortAnswerStorageDataT>,
        solutionTexts: Map<Int, String>
    ): QuizQuestionData.ShortAnswerData
}
