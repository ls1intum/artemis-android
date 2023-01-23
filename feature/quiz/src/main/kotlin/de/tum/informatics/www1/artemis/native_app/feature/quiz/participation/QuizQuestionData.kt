package de.tum.informatics.www1.artemis.native_app.feature.quiz.participation

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion

sealed class QuizQuestionData<T : QuizQuestion>(val question: T) {

    sealed interface EditableData

    sealed interface ResultData {
        val achievedPoints: Double
    }

    sealed class ShortAnswerData(
        question: ShortAnswerQuizQuestion,
        val solutionTexts: Map<Int, String>,
    ) : QuizQuestionData<ShortAnswerQuizQuestion>(question) {
        class Editable(
            question: ShortAnswerQuizQuestion,
            solutionTexts: Map<Int, String>,
            val onUpdateSolutionText: (spotId: Int, newSolutionText: String) -> Unit
        ) : ShortAnswerData(question, solutionTexts), EditableData

        class Result(
            question: ShortAnswerQuizQuestion,
            solutionTexts: Map<Int, String>,
            override val achievedPoints: Double
        ) : ShortAnswerData(question, solutionTexts), ResultData
    }

    sealed class MultipleChoiceData(
        question: MultipleChoiceQuizQuestion,
        val optionSelectionMapping: Map<Long, Boolean>,
    ) : QuizQuestionData<MultipleChoiceQuizQuestion>(question) {
        class Editable(
            question: MultipleChoiceQuizQuestion,
            optionSelectionMapping: Map<Long, Boolean>,
            val onRequestChangeAnswerOptionSelectionState: (Long, Boolean) -> Unit
        ) : MultipleChoiceData(question, optionSelectionMapping), EditableData

        class Result(
            question: MultipleChoiceQuizQuestion,
            optionSelectionMapping: Map<Long, Boolean>,
            override val achievedPoints: Double
        ) : MultipleChoiceData(question, optionSelectionMapping), ResultData
    }

    /**
     * @property availableDragItems the items that are still freely available
     * @property dropLocationMapping holds an entry for every drop location that has a drag item placed on it
     */
    sealed class DragAndDropData(
        question: DragAndDropQuizQuestion,
        val availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
        val dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>
    ) : QuizQuestionData<DragAndDropQuizQuestion>(question) {
        class Editable(
            question: DragAndDropQuizQuestion,
            availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
            dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
            val onDragItemIntoDropLocation: (itemId: Long, dropId: Long) -> Unit,
            val onSwapDropLocations: (oldDropLocationId: Long, newDropLocationId: Long) -> Unit,
            val onClearDropLocation: (dropId: Long) -> Unit
        ) : DragAndDropData(
            question, availableDragItems, dropLocationMapping
        ), EditableData

        class Result(
            question: DragAndDropQuizQuestion,
            availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
            dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
            override val achievedPoints: Double
        ) : DragAndDropData(
            question, availableDragItems, dropLocationMapping
        ), ResultData
    }
}