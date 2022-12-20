package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

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
        question: MultipleChoiceQuizQuestion
    ): QuizQuestionData<MultipleChoiceQuizQuestion>(question)

    class DragAndDropData(
        question: DragAndDropQuizQuestion
    ) : QuizQuestionData<DragAndDropQuizQuestion>(question)
}