package de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Result
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed class Participation {
    abstract val id: Int?
    abstract val initializationState: InitializationState?
    abstract val initializationDate: Instant?
    abstract val individualDueDate: Instant?
    abstract val results: List<Result>?
    abstract val exercise: Exercise?
    abstract val type: ParticipationType?

    enum class InitializationState {
        UNINITIALIZED,
        REPO_COPIED,
        REPO_CONFIGURED,
        BUILD_PLAN_COPIED,
        BUILD_PLAN_CONFIGURED,

        /**
         * The participation is set up for submissions from the student
         */
        INITIALIZED,
        /**
         * Text- / Modelling: At least one submission is done. Quiz: No further submissions should be possible
         */
        FINISHED,
        INACTIVE
    }

    // IMPORTANT NOTICE: The following strings have to be consistent with the ones defined in Participation.java
    enum class ParticipationType {
        @SerialName("student")
        STUDENT,
        @SerialName("programming")
        PROGRAMMING,
        @SerialName("template")
        TEMPLATE,
        @SerialName("solution")
        SOLUTION
    }
}