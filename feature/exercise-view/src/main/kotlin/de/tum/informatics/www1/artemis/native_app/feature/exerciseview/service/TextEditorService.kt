package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

interface TextEditorService : ArtemisContextBasedService {

    suspend fun getParticipation(participationId: Long): NetworkResponse<Participation>
}