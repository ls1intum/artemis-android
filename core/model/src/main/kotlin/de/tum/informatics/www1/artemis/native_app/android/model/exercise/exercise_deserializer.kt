package de.tum.informatics.www1.artemis.native_app.android.model.exercise

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val exerciseSerializerModule = SerializersModule {
    polymorphic(Exercise::class) {
        subclass(TextExercise::class)
        subclass(ModelingExercise::class)
        subclass(FileUploadExercise::class)
        subclass(ProgrammingExercise::class)
        subclass(QuizExercise::class)
        defaultDeserializer { UnknownExercise.serializer() }
    }
}