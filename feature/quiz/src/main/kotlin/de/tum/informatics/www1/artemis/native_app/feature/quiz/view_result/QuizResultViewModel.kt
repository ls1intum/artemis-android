package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import android.provider.ContactsContract.Data
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.feature.quiz.*
import de.tum.informatics.www1.artemis.native_app.feature.quiz.BaseQuizViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import kotlinx.coroutines.flow.*

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
        is QuizType.PracticeResults -> emptyFlow()
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
        QuizType.ViewResults -> submission.map { submissionDataState ->
            submissionDataState.transform { submission ->
                val result = submission
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

    override val dragAndDropData: Flow<Map<Long, ResultDragAndDropStorageData>> =
        submission.map { submissionDataState ->
            submissionDataState
                .bind { submission ->
                    submission
                        .submittedAnswers
                        .filterIsInstance<DragAndDropSubmittedAnswer>()
                        .filter { it.quizQuestion is DragAndDropQuizQuestion }
                        .associate { submittedAnswer ->
                            val question = submittedAnswer.quizQuestion as DragAndDropQuizQuestion

                            val value = submittedAnswer
                                .mappings
                                .mapNotNull {
                                    val dropLocationId =
                                        it.dropLocation?.id ?: return@mapNotNull null
                                    val dragItemId = it.dragItem?.id ?: return@mapNotNull null
                                    dropLocationId to dragItemId
                                }
                                .toMap()

                            val data = ResultDragAndDropStorageData(
                                value = value,
                                score = submittedAnswer.scoreInPoints ?: 0.0
                            )

                            question.id to data
                        }
                }
        }
            .filterSuccess()
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

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
            score = storageData?.score ?: 0.0
        )
    }

    data class ResultDragAndDropStorageData(
        override val value: Map<DropLocationId, DragItemId>,
        val score: Double
    ) : DragAndDropStorageData

    data class ResultMultipleChoiceStorageData(
        override val value: Map<AnswerOptionId, Boolean>,
        val score: Double
    ) : MultipleChoiceStorageData

    data class ResultShortAnswerStorageData(
        override val value: Map<Int, String>,
        val score: Double
    ) : ShortAnswerStorageData
}