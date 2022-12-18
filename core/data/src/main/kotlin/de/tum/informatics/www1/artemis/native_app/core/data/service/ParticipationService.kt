package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface ParticipationService {

    /**
     * Finds one participation for the given exercise
     */
    suspend fun findParticipation(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Participation>
}