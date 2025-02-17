package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import kotlinx.coroutines.flow.Flow

interface ServerTimeService : ArtemisContextBasedService {

    /**
     * A clock approximating the server time.
     */
    fun getServerClock(): Flow<ClockWithOffset>
}
