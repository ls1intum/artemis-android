package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("short-answer")
data class ShortAnswerQuizQuestion(
    override val id: Long = 0,
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
    val correctMappings: List<ShortAnswerMapping>? = null,
    val matchLetterCase: Boolean = false,
    val similarityValue: Int = 85
) : QuizQuestion() {
    @Serializable
    data class ShortAnswerSpot(
        val id: Long? = null,
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

    @Serializable
    data class ShortAnswerMapping(
        val id: Long? = null,
        val shortAnswerSpotIndex: Long? = null,
        val shortAnswerSolutionIndex: Long? = null,
        val solution: ShortAnswerSolution? = null,
        val spot: ShortAnswerSpot? = null
    )
}