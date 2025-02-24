package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface ParticipationService : ArtemisContextBasedService {

    /**
     * Finds one participation for the given exercise
     */
    suspend fun findParticipation(exerciseId: Long): NetworkResponse<Participation>
}