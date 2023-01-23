package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val submittedAnswerSerializerModule = SerializersModule {
    polymorphic(SubmittedAnswer::class) {
        subclass(DragAndDropSubmittedAnswer::class)
        subclass(MultipleChoiceSubmittedAnswer::class)
        subclass(ShortAnswerSubmittedAnswer::class)
    }
}