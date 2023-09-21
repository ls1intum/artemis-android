package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import kotlinx.coroutines.flow.Flow

interface BetaHintService {

    val shouldShowBetaHint: Flow<Boolean>

    suspend fun dismissBetaHintPermanently()
}
