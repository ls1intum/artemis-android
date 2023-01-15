package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import de.tum.informatics.www1.artemis.native_app.core.common.ClockWithOffset
import de.tum.informatics.www1.artemis.native_app.core.common.offsetBy
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.websocket.ServerTimeService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    private val ktorProvider: KtorProvider,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService
) :
    ServerTimeService {

    @OptIn(DelicateCoroutinesApi::class)
    override val serverClock: Flow<ClockWithOffset> = combine(
        serverConfigurationService.serverUrl,
        accountService.authToken
    ) { serverUrl, authToken ->
        serverUrl to authToken
    }
        .transformLatest { (serverUrl, authToken) ->
            // Logic of implementation taken from: https://gamedev.stackexchange.com/a/93662

            val firstResponse = performClockSync(Clock.System, serverUrl, authToken)
                ?: throw TimeSyncFailedException()

            val initiallyAdjustedClock =
                Clock.System.offsetBy(firstResponse.improvedDelta.milliseconds)
            emit(initiallyAdjustedClock)

            val results = mutableListOf<SyncResult>()

            // Sync clock up to 5 times, emitting a new value every time.
            for (i in 0 until 5) {
                delay(1.seconds)
                results += performClockSync(initiallyAdjustedClock, serverUrl, authToken)
                    ?: throw TimeSyncFailedException()

                val sortedResults = results.sortedBy { it.latencyInMs }

                val median = sortedResults[results.size / 2]
                print(median)

                if (results.size >= 1) {
                    val stdDeviation = sqrt(results.sumOf {
                        val t = it.latencyInMs - median.latencyInMs
                        t * t
                    }.toFloat() / results.size.toFloat())

                    print(stdDeviation)

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
        .shareIn(GlobalScope, SharingStarted.WhileSubscribed(1.seconds), replay = 1)

    private suspend fun performClockSync(
        clock: Clock,
        serverUrl: String,
        authToken: String
    ): SyncResult? {
        return when (val serverTimeResponse = requestServerTime(clock, serverUrl, authToken)) {
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

    private suspend fun requestServerTime(
        clock: Clock,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ServerTimeRequestResponse> {
        return retryNetworkCall(maxRetries = 3, delayBetweenRetries = 1.seconds) {
            val sentTime = clock.now().toEpochMilliseconds()
            performNetworkCall {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments("time")
                    }

                    contentType(ContentType.Application.Json)
                    bearerAuth(authToken)
                }.body<Instant>()
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