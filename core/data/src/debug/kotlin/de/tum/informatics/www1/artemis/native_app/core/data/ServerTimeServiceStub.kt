package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.common.offsetBy
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlin.time.Duration

class ServerTimeServiceStub(
    private val serverClock: ClockWithOffset = Clock.System.offsetBy(Duration.ZERO)
) : ServerTimeService {
    override fun getServerClock(authToken: String, serverUrl: String): Flow<ClockWithOffset> =
        flowOf(serverClock)
}
