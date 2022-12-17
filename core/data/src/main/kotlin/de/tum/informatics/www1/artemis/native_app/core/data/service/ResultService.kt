package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback
import kotlinx.coroutines.flow.Flow

interface ResultService {
    fun getFeedbackDetailsForResult(
        participationId: Long,
        resultId: Long,
        serverUrl: String,
        bearerToken: String
    ): Flow<DataState<List<Feedback>>>
}