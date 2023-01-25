package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback

interface ResultService {
    suspend fun getFeedbackDetailsForResult(
        participationId: Long,
        resultId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<Feedback>>
}