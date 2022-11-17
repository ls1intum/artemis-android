package de.tum.informatics.www1.artemis.native_app.android.model.exercise.submission

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val submissionSerializerModule = SerializersModule {
    polymorphic(Submission::class) {
        subclass(InstructorSubmission::class)
        subclass(TestSubmission::class)
        defaultDeserializer { UnknownSubmission.serializer() }
    }
}