package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface TextEditorService {

    suspend fun getParticipation(
        participationId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Participation>
}