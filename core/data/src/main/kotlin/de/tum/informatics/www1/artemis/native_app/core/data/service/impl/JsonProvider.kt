package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.exerciseSerializerModule
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.participationSerializerModule
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.quizQuestionSerializerModule
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.submittedAnswerSerializerModule
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.submissionSerializerModule
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lectureSerializerModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class JsonProvider {

    /**
     * The response json configuration.
     */
    val applicationJsonConfiguration = Json {
        // Ignore unknown keys. If set to false, parsing JSON will throw an error when a new key is introduced.
        ignoreUnknownKeys = true

        encodeDefaults = true // Values that are not set are filled with the defaults given in the constructor.
        coerceInputValues = true
        explicitNulls = false

        serializersModule = SerializersModule {
            include(exerciseSerializerModule)
            include(lectureSerializerModule)
            include(participationSerializerModule)
            include(submissionSerializerModule)
            include(quizQuestionSerializerModule)
            include(submittedAnswerSerializerModule)
        }
    }
}
