package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("multiple-choice")
class MultipleChoiceSubmittedAnswer(
    override val id: Long? = null,
    override val scoreInPoints: Double? = null,
    override val quizQuestion: QuizQuestion? = null,
    val selectedOptions: List<MultipleChoiceQuizQuestion.AnswerOption> = emptyList()
) : SubmittedAnswer()