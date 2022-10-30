package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.exerciseSerializerModule
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.participationSerializerModule
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.submissionSerializerModule
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.lectureSerializerModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class JsonProvider {

    /**
     * The response json configuration.
     */
    val networkJsonConfiguration = Json {
        //Ignore unknown keys. If set to false, parsing JSON will throw an error when a new key is introduced.
        ignoreUnknownKeys = true

        encodeDefaults = true //Values that are not set are filled with the defaults given in the constructor.

        serializersModule = SerializersModule {
            include(exerciseSerializerModule)
            include(lectureSerializerModule)
            include(participationSerializerModule)
            include(submissionSerializerModule)
        }
    }
}