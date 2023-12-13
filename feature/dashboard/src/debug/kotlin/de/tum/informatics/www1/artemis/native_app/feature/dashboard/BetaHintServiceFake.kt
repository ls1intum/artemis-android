package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.BetaHintService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class BetaHintServiceFake(override val shouldShowBetaHint: Flow<Boolean> = flowOf(false)) : BetaHintService {

    override suspend fun dismissBetaHintPermanently() = Unit
}
