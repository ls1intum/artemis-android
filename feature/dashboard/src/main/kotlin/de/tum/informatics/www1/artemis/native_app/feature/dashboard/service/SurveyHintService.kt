package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import kotlinx.coroutines.flow.Flow

interface SurveyHintService {

    val shouldShowSurveyHint: Flow<Boolean>

    suspend fun dismissSurveyHintPermanently()
}