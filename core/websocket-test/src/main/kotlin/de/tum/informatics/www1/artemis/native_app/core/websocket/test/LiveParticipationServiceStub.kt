package de.tum.informatics.www1.artemis.native_app.core.websocket.test

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class LiveParticipationServiceStub : LiveParticipationService {
    override val personalSubmissionUpdater: Flow<Result> = emptyFlow()

    override fun getLatestPendingSubmissionByParticipationIdFlow(
        participationId: Long,
        exerciseId: Long,
        personal: Boolean,
        fetchPending: Boolean
    ): Flow<LiveParticipationService.ProgrammingSubmissionStateData?> = emptyFlow()

    override fun subscribeForParticipationChanges(): Flow<StudentParticipation> = emptyFlow()
}
