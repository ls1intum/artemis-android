package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface ParticipationService : LoggedInBasedService {

    /**
     * Returns the current user's participation in the given quiz exercise, together with the exercise
     * itself and the submissions made so far.
     *
     * The server creates the participation on the first call and returns the existing one afterwards,
     * so calling this repeatedly is safe; it is how the quiz is loaded in live and view-results mode.
     */
    suspend fun findParticipation(exerciseId: Long): NetworkResponse<Participation>
}
