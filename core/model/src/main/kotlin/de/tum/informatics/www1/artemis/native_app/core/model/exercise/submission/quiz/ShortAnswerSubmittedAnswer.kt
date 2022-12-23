package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("short-answer")
class ShortAnswerSubmittedAnswer(
    override val id: Long? = null,
    override val scoreInPoints: Double? = null,
    override val quizQuestion: QuizQuestion? = null,
    val submittedTexts: List<ShortAnswerSubmittedText> = emptyList()
) : SubmittedAnswer() {

    @Serializable
    data class ShortAnswerSubmittedText(
        val id: Long? = null,
        val text: String? = null,
        val isCorrect: Boolean? = null,
        val spot: ShortAnswerQuizQuestion.ShortAnswerSpot? = null
    )
}