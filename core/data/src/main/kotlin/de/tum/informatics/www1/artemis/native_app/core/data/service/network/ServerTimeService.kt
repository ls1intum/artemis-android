package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import kotlinx.coroutines.flow.Flow

interface ServerTimeService {

    /**
     * A clock approximating the server time.
     */
    fun getServerClock(authToken: String, serverUrl: String): Flow<ClockWithOffset>
}
