package de.tum.informatics.www1.artemis.native_app.core.websocket

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import kotlinx.coroutines.flow.Flow

interface ServerTimeService {

    /**
     * A clock approximating the server time.
     */
    val serverClock: Flow<ClockWithOffset>
}