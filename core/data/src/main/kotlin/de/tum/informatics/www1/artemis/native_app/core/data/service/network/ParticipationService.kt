package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface ParticipationService : LoggedInBasedService {

    /**
     * Finds one participation for the given exercise
     */
    suspend fun findParticipation(exerciseId: Long): NetworkResponse<Participation>
}