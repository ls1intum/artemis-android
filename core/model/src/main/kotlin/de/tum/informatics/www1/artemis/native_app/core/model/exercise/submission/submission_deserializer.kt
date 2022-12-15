package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalSerializationApi::class)
val submissionSerializerModule = SerializersModule {
    polymorphic(Submission::class) {
        subclass(InstructorSubmission::class)
        subclass(TestSubmission::class)
        subclass(ProgrammingSubmission::class)
        subclass(TextSubmission::class)
        defaultDeserializer { UnknownSubmission.serializer() }
    }
}