package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission

internal interface TextSubmissionService : LoggedInBasedService {

    suspend fun update(
        textSubmission: TextSubmission,
        exerciseId: Long,
        session: ArtemisContext.LoggedIn? = null
    ): NetworkResponse<TextSubmission>
}
