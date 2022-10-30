package de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val submissionSerializerModule = SerializersModule {
    polymorphic(Submission::class) {
        defaultDeserializer { UnknownSubmission.serializer() }
    }
}