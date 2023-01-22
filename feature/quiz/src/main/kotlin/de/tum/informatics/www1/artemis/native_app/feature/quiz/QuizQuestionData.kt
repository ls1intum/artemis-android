package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion

sealed class QuizQuestionData<T : QuizQuestion>(val question: T) {
    class ShortAnswerData(
        question: ShortAnswerQuizQuestion,
        val solutionTexts: Map<Int, String>,
        val onUpdateSolutionText: (spotId: Int, newSolutionText: String) -> Unit
    ) : QuizQuestionData<ShortAnswerQuizQuestion>(question)

    class MultipleChoiceData(
        question: MultipleChoiceQuizQuestion,
        val optionSelectionMapping: Map<Long, Boolean>,
        val onRequestChangeAnswerOptionSelectionState: (Long, Boolean) -> Unit
    ) : QuizQuestionData<MultipleChoiceQuizQuestion>(question)

    /**
     * @property availableDragItems the items that are still freely available
     * @property dropLocationMapping holds an entry for every drop location that has a drag item placed on it
     */
    class DragAndDropData(
        question: DragAndDropQuizQuestion,
        val availableDragItems: List<DragAndDropQuizQuestion.DragItem>,
        val dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
        val onDragItemIntoDropLocation: (itemId: Long, dropId: Long) -> Unit,
        val onSwapDropLocations: (oldDropLocationId: Long, newDropLocationId: Long) -> Unit,
        val onClearDropLocation: (dropId: Long) -> Unit
    ) : QuizQuestionData<DragAndDropQuizQuestion>(question)
}