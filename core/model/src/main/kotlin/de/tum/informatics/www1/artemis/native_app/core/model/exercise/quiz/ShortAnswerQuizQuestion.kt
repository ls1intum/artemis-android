package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("short-answer")
data class ShortAnswerQuizQuestion(
    override val id: Long? = null,
    override val title: String? = null,
    override val text: String? = null,
    override val hint: String? = null,
    override val explanation: String? = null,
    override val points: Int? = null,
    override val scoringType: ScoringType? = null,
    override val randomizeOrder: Boolean = true,
    override val invalid: Boolean = false,
    val spots: List<ShortAnswerSpot> = emptyList(),
    val solutions: List<ShortAnswerSolution> = emptyList(),
    val matchLetterCase: Boolean = false,
    val similarityValue: Int = 85
) : QuizQuestion() {
    @Serializable
    data class ShortAnswerSpot(
        val width: Int? = null,
        val spotNr: Int? = null,
        val invalid: Boolean = false
    )

    @Serializable
    data class ShortAnswerSolution(
        val id: Long? = null,
        val text: String? = null,
        val invalid: Boolean = false
    )
}