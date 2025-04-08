package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import kotlinx.coroutines.flow.Flow

interface ServerTimeService : LoggedInBasedService {

    /**
     * A clock approximating the server time.
     */
    fun getServerClock(): Flow<ClockWithOffset>
}
