package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.offsetBy
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Implements the time sync logic using the stackoverflow answer given here: https://gamedev.stackexchange.com/a/93662
 */
internal class ServerTimeServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider),  ServerTimeService {

    override fun getServerClock(): Flow<ClockWithOffset> = flow {
        // Logic of implementation taken from: https://gamedev.stackexchange.com/a/93662

        val firstResponse = performClockSync(clock = Clock.System) ?: throw TimeSyncFailedException()

        val initiallyAdjustedClock =
            Clock.System.offsetBy(firstResponse.improvedDelta.milliseconds)
        emit(initiallyAdjustedClock)

        val results = mutableListOf<SyncResult>()

        // Sync clock up to 5 times, emitting a new value every time.
        for (i in 0 until 5) {
            delay(1.seconds)
            results += performClockSync(clock = initiallyAdjustedClock) ?: throw TimeSyncFailedException()

            val sortedResults = results.sortedBy { it.latencyInMs }

            val median = sortedResults[results.size / 2]

            if (results.size >= 1) {
                val stdDeviation = sqrt(results.sumOf {
                    val t = it.latencyInMs - median.latencyInMs
                    t * t
                }.toFloat() / results.size.toFloat())

                // 0.01f rounding error tolerance.
                val remainingPackets =
                    sortedResults.filter { abs(it.latencyInMs - median.latencyInMs) < stdDeviation + 0.01f }

                val delta = remainingPackets.sumOf { it.improvedDelta } / remainingPackets.size
                emit(initiallyAdjustedClock.offsetBy(delta.milliseconds))
            } else {
                emit(initiallyAdjustedClock.offsetBy(median.improvedDelta.milliseconds))
            }
        }
    }
        .onStart { emit(Clock.System.offsetBy(Duration.ZERO)) }
        .retryWhen { cause, attempt ->
            // Only retry when a time sync exception happened
            if (cause is TimeSyncFailedException) {
                delay(attempt.seconds)
                true
            } else false
        }

    private suspend fun performClockSync(clock: Clock, ): SyncResult? {
        return when (val serverTimeResponse = requestServerTime(clock = clock)) {
            is NetworkResponse.Response -> {
                val sentTime = serverTimeResponse.data.sentTime
                val serverTime = serverTimeResponse.data.serverTime
                val arrivalTime = clock.now().toEpochMilliseconds()

                val latency = (arrivalTime - sentTime) / 2

                val improvedDelta = arrivalTime - serverTime + latency / 2
                SyncResult(latency, -improvedDelta)
            }

            is NetworkResponse.Failure -> {
                return null
            }
        }
    }

    private suspend fun requestServerTime(clock: Clock): NetworkResponse<ServerTimeRequestResponse> {
        return retryNetworkCall(maxRetries = 3, delayBetweenRetries = 1.seconds) {
            val sentTime = clock.now().toEpochMilliseconds()
            getRequest<Instant> {
                url {
                    appendPathSegments(*Api.Core.Public.path, "time")
                }
            }
                .bind { serverTime ->
                    ServerTimeRequestResponse(sentTime, serverTime.toEpochMilliseconds())
                }
        }
    }

    private data class ServerTimeRequestResponse(val sentTime: Long, val serverTime: Long)
    private data class SyncResult(val latencyInMs: Long, val improvedDelta: Long)

    private class TimeSyncFailedException : Exception()
}