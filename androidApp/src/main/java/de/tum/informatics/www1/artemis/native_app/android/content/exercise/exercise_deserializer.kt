package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val exerciseSerializerModule = SerializersModule {
    polymorphic(Exercise::class) {
        subclass(TextExercise::class)
        subclass(ModelingExercise::class)
        defaultDeserializer { UnknownExercise.serializer() }
    }
}