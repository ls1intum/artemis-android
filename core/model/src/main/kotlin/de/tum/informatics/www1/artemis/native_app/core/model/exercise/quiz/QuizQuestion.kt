package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class QuizQuestion {
    abstract val id: Long
    abstract val title: String?
    abstract val text: String?
    abstract val hint: String?
    abstract val explanation: String?
    abstract val points: Int?
    abstract val scoringType: ScoringType?
    abstract val randomizeOrder: Boolean
    abstract val invalid: Boolean

    enum class ScoringType {
        ALL_OR_NOTHING,
        PROPORTIONAL_WITH_PENALTY,
        PROPORTIONAL_WITHOUT_PENALTY
    }
}