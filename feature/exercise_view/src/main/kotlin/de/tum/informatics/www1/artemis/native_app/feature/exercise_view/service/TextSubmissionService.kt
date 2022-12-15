package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission

internal interface TextSubmissionService {

    suspend fun create(
        textSubmission: TextSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<TextSubmission>

    suspend fun update(
        textSubmission: TextSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<TextSubmission>
}