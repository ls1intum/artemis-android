package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("multiple-choice")
data class MultipleChoiceQuizQuestion(
    override val id: Long? = null,
    override val title: String? = null,
    override val text: String? = null,
    override val hint: String? = null,
    override val explanation: String? = null,
    override val points: Int? = null,
    override val scoringType: ScoringType? = null,
    override val randomizeOrder: Boolean = true,
    override val invalid: Boolean = false,
    val answerOptions: List<AnswerOption> = emptyList(),
    val hasCorrectOption: Boolean = false,
    val singleChoice: Boolean = false
) : QuizQuestion() {

    @Serializable
    data class AnswerOption(
        val id: Long? = null,
        val text: String? = null,
        val hint: String? = null,
        val explanation: String? = null,
        val isCorrect: Boolean = false,
        val invalid: Boolean = false
    )
}
