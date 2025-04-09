package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.SurveyHintService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SurveyHintServiceFake : SurveyHintService {
    override val shouldShowSurveyHint: Flow<Boolean> = flowOf(false)

    override suspend fun dismissSurveyHintPermanently() {
        // No-op
    }
}