package de.tum.informatics.www1.artemis.native_app.android.model.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.submission.Submission
import kotlinx.datetime.Instant
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
    abstract val submissions: List<Submission>?

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

    /**
     * Check if a given participation is in due time of the given exercise based on its submission at index position 0.
     * Before the method is called, it must be ensured that the submission at index position 0 is suitable to check if
     * the participation is in due time of the exercise.
     * From: https://github.com/ls1intum/Artemis/blob/310aa64d55c1347b4c2cf6367be551ce1d8f9a4a/src/main/webapp/app/exercises/shared/participation/participation.utils.ts#L87
     */
    fun isInDueTime(associatedExercise: Exercise? = exercise): Boolean {
        // If the exercise has no dueDate set, every submission is in time.
        if (associatedExercise?.dueDate == null) return true

        // If the participation has no submission, it cannot be in due time.
        if (submissions.orEmpty().isEmpty()) return false

        // If the submissionDate is before the dueDate of the exercise, the submission is in time.
        val submission = submissions!![0]
        val submissionDate = submission.submissionDate
        if (submissionDate != null) {
            return submissionDate < (associatedExercise.getDueDate(this) ?: return true)
        }

        // If the submission has no submissionDate set, the submission cannot be in time.
        return false
    }
}