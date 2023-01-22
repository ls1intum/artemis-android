package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface QuizType {

    @Serializable
    sealed interface WorkableQuizType : QuizType

    sealed interface ViewableQuizType : QuizType

    @Serializable
    @SerialName("live")
    object Live : WorkableQuizType

    @Serializable
    @SerialName("practice")
    object Practice : WorkableQuizType

    object ViewResults : ViewableQuizType

    data class PracticeResults(val result: Result) : ViewableQuizType
}




