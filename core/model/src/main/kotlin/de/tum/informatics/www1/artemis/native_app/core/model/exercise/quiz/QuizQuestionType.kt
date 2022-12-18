package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QuizQuestionType(val serialName: String) {
    @SerialName("multiple-choice")
    MULTIPLE_CHOICE("multiple-choice"),

    @SerialName("drag-and-drop")
    DRAG_AND_DROP("drag-and-drop"),

    @SerialName("short-answer")
    SHORT_ANSWER("short-answer")
}