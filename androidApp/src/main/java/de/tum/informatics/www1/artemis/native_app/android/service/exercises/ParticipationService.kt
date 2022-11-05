package de.tum.informatics.www1.artemis.native_app.android.service.exercises

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Submission
import kotlinx.coroutines.flow.Flow

interface ParticipationService {

    /**
     * Subscribed to the users personal participations
     */
    val personalSubmissionUpdater: Flow<Submission>

    /**
     * @param isPersonalParticipation whether the participation belongs to the user (by being a student) or not (by being an instructor)
     */
    fun getLatestPendingSubmissionByParticipationIdFlow(
        participationId: Int,
        exerciseId: Int,
        isPersonalParticipation: Boolean,
        personal: Boolean,
        fetchPending: Boolean = true
    ): Flow<ProgrammingSubmissionStateData?>

    /**
     * Subscribing for general changes in a participation object. This will triggered if a new result is received by the service.
     * A received object will be the full participation object including all results and the exercise.
     *
     * **See also:** [js source](https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/participation-websocket.service.ts#L228)
     */
    fun subscribeForParticipationChanges(): Flow<StudentParticipation>

    sealed class ProgrammingSubmissionStateData(
        val participationId: Int
    ) {
        // The last submission of participation has a result.
        class NoPendingSubmission(participationId: Int) :
            ProgrammingSubmissionStateData(participationId)

        // The submission was created on the server, we assume that the build is running within an expected time frame.
        class IsBuildingPendingSubmission(participationId: Int, val submission: Submission) :
            ProgrammingSubmissionStateData(participationId)

        // A failed submission is a pending submission that has not received a result within an expected time frame.
        class FailedSubmission(participationId: Int) :
            ProgrammingSubmissionStateData(participationId)
    }
}