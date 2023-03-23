package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.quiz.AnswerOptionId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.BaseQuizViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DragAndDropStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DragItemId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.DropLocationId
import de.tum.informatics.www1.artemis.native_app.feature.quiz.MultipleChoiceStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.ShortAnswerStorageData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn

internal class QuizResultViewModel(
    exerciseId: Long,
    quizType: QuizType.ViewableQuizType,
    quizExerciseService: QuizExerciseService,
    networkStatusProvider: NetworkStatusProvider,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    participationService: ParticipationService
) : BaseQuizViewModel<QuizResultViewModel.ResultShortAnswerStorageData, QuizResultViewModel.ResultDragAndDropStorageData, QuizResultViewModel.ResultMultipleChoiceStorageData>(
    exerciseId,
    quizType,
    quizExerciseService,
    networkStatusProvider,
    serverConfigurationService,
    accountService,
    participationService
) {

    val submission: StateFlow<DataState<QuizSubmission>> = when (quizType) {
        is QuizType.PracticeResults -> if (quizType.result.submission as? QuizSubmission != null) {
            flowOf(DataState.Success(quizType.result.submission as QuizSubmission))
        } else flowOf(DataState.Failure(RuntimeException("No submission provide")))

        QuizType.ViewResults -> initialParticipationDataState
            .mapNotNull { initialParticipationDataState ->
                initialParticipationDataState.transform { initialParticipation ->
                    val submission = initialParticipation
                        .results
                        .orEmpty()
                        .firstOrNull()
                        ?.submission as? QuizSubmission

                    if (submission == null) {
                        DataState.Failure(RuntimeException("No submission loaded"))
                    } else DataState.Success(submission)
                }
            }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    val result: StateFlow<DataState<Result>> = when (quizType) {
        is QuizType.PracticeResults -> flowOf(DataState.Success(quizType.result))
        QuizType.ViewResults -> initialParticipationDataState.map { participationDataState ->
            participationDataState.transform { participation ->
                val result = participation
                    .results
                    .orEmpty()
                    .firstOrNull()

                if (result == null) {
                    DataState.Failure(RuntimeException("No result loaded"))
                } else DataState.Success(result)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    private val submissionAndQuizExerciseDataState: Flow<DataState<Pair<QuizSubmission, List<QuizQuestion>>>> =
        combine(submission, quizExerciseDataState) { submission, quizExercise ->
            submission join quizExercise.bind { it.quizQuestions }
        }

    override val dragAndDropData: Flow<Map<Long, ResultDragAndDropStorageData>> =
        submissionAndQuizExerciseDataState
            .map { dataState ->
                dataState
                    .bind { (submission, quizQuestions) ->
                        submission
                            .submittedAnswers
                            .filterIsInstance<DragAndDropSubmittedAnswer>()
                            .mapNotNull { submittedAnswer ->
                                val question =
                                    quizQuestions.firstOrNull { it.id == submittedAnswer.quizQuestion?.id } as? DragAndDropQuizQuestion
                                        ?: return@mapNotNull null

                                val value = submittedAnswer
                                    .mappings
                                    .mapNotNull innerMappings@{
                                        val dropLocationId =
                                            it.dropLocation?.id ?: return@innerMappings null
                                        val dragItemId =
                                            it.dragItem?.id ?: return@innerMappings null
                                        dropLocationId to dragItemId
                                    }
                                    .toMap()

                                val data = ResultDragAndDropStorageData(
                                    value = value,
                                    achievedPoints = submittedAnswer.scoreInPoints ?: 0.0
                                )

                                question.id to data
                            }
                            .toMap()
                    }
            }
            .filterSuccess()
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    override val multipleChoiceData: Flow<Map<Long, ResultMultipleChoiceStorageData>> =
        submissionAndQuizExerciseDataState
            .map { dataState ->
                dataState.bind { (submission, quizQuestions) ->
                    submission
                        .submittedAnswers
                        .filterIsInstance<MultipleChoiceSubmittedAnswer>()
                        .mapNotNull { submittedAnswer ->
                            val question =
                                quizQuestions.firstOrNull { it.id == submittedAnswer.quizQuestion?.id } as? MultipleChoiceQuizQuestion
                                    ?: return@mapNotNull null

                            val value = submittedAnswer
                                .selectedOptions
                                .associate {
                                    it.id to true
                                }

                            val data = ResultMultipleChoiceStorageData(
                                value = value,
                                achievedPoints = submittedAnswer.scoreInPoints ?: 0.0
                            )

                            question.id to data
                        }
                        .toMap()
                }
            }
            .filterSuccess()
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    override val shortAnswerData: Flow<Map<Long, ResultShortAnswerStorageData>> =
        submissionAndQuizExerciseDataState
            .map { dataState ->
                dataState.bind { (submission, quizQuestions) ->
                    submission
                        .submittedAnswers
                        .filterIsInstance<ShortAnswerSubmittedAnswer>()
                        .mapNotNull { submittedAnswer ->
                            val question =
                                quizQuestions.firstOrNull { it.id == submittedAnswer.quizQuestion?.id } as? ShortAnswerQuizQuestion
                                    ?: return@mapNotNull null

                            val value = submittedAnswer
                                .submittedTexts
                                .mapNotNull innerMapping@{
                                    val spotId = it.spot?.spotNr ?: return@innerMapping null

                                    spotId to it.text.orEmpty()
                                }
                                .toMap()

                            val data = ResultShortAnswerStorageData(
                                value = value,
                                achievedPoints = submittedAnswer.scoreInPoints ?: 0.0
                            )

                            question.id to data
                        }
                        .toMap()
                }
            }
            .filterSuccess()
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    override fun constructDragAndDropData(
        questionId: Long,
        question: DragAndDropQuizQuestion,
        storageData: ResultDragAndDropStorageData?,
        availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
        dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>
    ): QuizQuestionData.DragAndDropData {
        return QuizQuestionData.DragAndDropData.Result(
            question = question,
            availableDragItems = availableDragItems,
            dropLocationMapping = dropLocationMapping,
            achievedPoints = storageData?.achievedPoints ?: 0.0
        )
    }

    override fun constructMultipleChoiceData(
        questionId: Long,
        question: MultipleChoiceQuizQuestion,
        storageData: ResultMultipleChoiceStorageData?,
        multipleChoiceData: Map<Long, ResultMultipleChoiceStorageData>,
        optionSelectionMapping: Map<AnswerOptionId, Boolean>
    ): QuizQuestionData.MultipleChoiceData {
        return QuizQuestionData.MultipleChoiceData.Result(
            question = question,
            optionSelectionMapping = optionSelectionMapping,
            achievedPoints = storageData?.achievedPoints ?: 0.0
        )
    }

    override fun constructShortAnswerData(
        questionId: Long,
        question: ShortAnswerQuizQuestion,
        storageData: ResultShortAnswerStorageData?,
        shortAnswerData: Map<Long, ResultShortAnswerStorageData>,
        solutionTexts: Map<Int, String>
    ): QuizQuestionData.ShortAnswerData {
        return QuizQuestionData.ShortAnswerData.Result(
            question = question,
            solutionTexts = solutionTexts,
            achievedPoints = storageData?.achievedPoints ?: 0.0
        )
    }

    data class ResultDragAndDropStorageData(
        override val value: Map<DropLocationId, DragItemId>,
        val achievedPoints: Double
    ) : DragAndDropStorageData

    data class ResultMultipleChoiceStorageData(
        override val value: Map<AnswerOptionId, Boolean>,
        val achievedPoints: Double
    ) : MultipleChoiceStorageData

    data class ResultShortAnswerStorageData(
        override val value: Map<Int, String>,
        val achievedPoints: Double
    ) : ShortAnswerStorageData
}