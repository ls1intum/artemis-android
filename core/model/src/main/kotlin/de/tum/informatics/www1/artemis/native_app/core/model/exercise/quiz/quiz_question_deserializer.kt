package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val quizQuestionSerializerModule = SerializersModule {
    polymorphic(QuizQuestion::class) {
        subclass(DragAndDropQuizQuestion::class)
        subclass(MultipleChoiceQuizQuestion::class)
        subclass(ShortAnswerQuizQuestion::class)
    }
}