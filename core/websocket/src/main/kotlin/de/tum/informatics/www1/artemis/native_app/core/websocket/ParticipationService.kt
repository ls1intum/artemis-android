package de.tum.informatics.www1.artemis.native_app.core.websocket

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import kotlinx.coroutines.flow.Flow

interface ParticipationService {

    /**
     *
     * Subscribed to the users personal participations
     */
    val personalSubmissionUpdater: Flow<Result>

    /**
     * Subscribe for the latest pending submission for the given participation.
     * A latest pending submission is characterized by the following properties:
     * - Submission is the newest one (by submissionDate)
     * - Submission does not have a result (yet)
     * - Submission is not older than DEFAULT_EXPECTED_RESULT_ETA (in this case it could be that never a result will come due to an error)
     *
     * Will emit:
     * - A submission if a last pending submission exists.
     * - An null value when there is not a pending submission.
     * - An null value when no result arrived in time for the submission.
     *
     * This method will execute a REST call to the server so that the subscriber will always receive the latest information from the server.
     *
     * @param participationId id of the ProgrammingExerciseStudentParticipation
     * @param exerciseId id of ProgrammingExercise
     * @param isPersonalParticipation whether the current user is a participant in the participation.
     * @param fetchPending whether the latest pending submission should be fetched from the server
     */
    fun getLatestPendingSubmissionByParticipationIdFlow(
        participationId: Long,
        exerciseId: Long,
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
        val participationId: Long
    ) {
        // The last submission of participation has a result.
        class NoPendingSubmission(participationId: Long) :
            ProgrammingSubmissionStateData(participationId)

        // The submission was created on the server, we assume that the build is running within an expected time frame.
        class IsBuildingPendingSubmission(participationId: Long, val submission: Submission) :
            ProgrammingSubmissionStateData(participationId)

        // A failed submission is a pending submission that has not received a result within an expected time frame.
        class FailedSubmission(participationId: Long) :
            ProgrammingSubmissionStateData(participationId)
    }
}