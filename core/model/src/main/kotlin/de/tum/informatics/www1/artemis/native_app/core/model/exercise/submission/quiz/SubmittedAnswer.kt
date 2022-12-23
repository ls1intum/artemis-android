package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestionType
import kotlinx.serialization.Serializable

@Serializable
sealed class SubmittedAnswer {
    abstract val id: Long?
    abstract val scoreInPoints: Double?
    abstract val quizQuestion: QuizQuestion?
}