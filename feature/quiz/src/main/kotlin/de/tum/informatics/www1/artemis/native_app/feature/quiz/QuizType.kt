package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface QuizType {

    @Serializable
    sealed interface WorkableQuizType : QuizType

    @Serializable
    sealed interface ViewableQuizType : QuizType

    @Serializable
    @SerialName("live")
    object Live : WorkableQuizType

    @Serializable
    @SerialName("practice")
    object Practice : WorkableQuizType

    @Serializable
    @SerialName("view-results")
    object ViewResults : ViewableQuizType

    @Serializable
    @SerialName("practice-results")
    data class PracticeResults(val quizExercise: QuizExercise, val result: Result) : ViewableQuizType
}
