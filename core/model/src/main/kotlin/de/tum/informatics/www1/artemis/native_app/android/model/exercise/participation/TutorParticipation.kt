package de.tum.informatics.www1.artemis.native_app.android.model.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.model.account.User
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.Exercise

/**
 * From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/entities/participation/tutor-participation.model.ts
 */
@kotlinx.serialization.Serializable
class TutorParticipation(
    val id: Int? = null,
    val status: TutorParticipationStatus? = null,
    val assessedExercise: Exercise? = null,
    val tutor: User?,
    //trainedExampleSubmission...
) {
    enum class TutorParticipationStatus {
        NOT_PARTICIPATED,
        REVIEWED_INSTRUCTIONS,
        TRAINED,
        COMPLETED,
    }
}