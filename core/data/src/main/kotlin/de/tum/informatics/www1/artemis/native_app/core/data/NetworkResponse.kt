package de.tum.informatics.www1.artemis.native_app.core.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Wrapper around network responses. Used to propagate failures correctly.
 */
sealed class NetworkResponse<T> {
    fun <K> bind(transform: (T) -> K): NetworkResponse<K> {
        return when (this) {
            is Failure -> Failure(exception)
            is Response -> Response(transform(data))
        }
    }

    fun or(fallback: T): T = when (this) {
        is Failure -> fallback
        is Response -> data
    }

    fun orNull(): T? = when (this) {
        is Failure -> null
        is Response -> data
    }

    data class Response<T>(val data: T) : NetworkResponse<T>()

    data class Failure<T>(val exception: Exception) : NetworkResponse<T>()
}

const val TAG = "NetworkResponse"

suspend inline fun <T> performNetworkCall(
    crossinline perform: suspend () -> T
): NetworkResponse<T> {
    return try {
        withTimeout(10.seconds) {
            NetworkResponse.Response(perform())
        }
    } catch (e: CancellationException) {
        // Hand through cancellation
        throw e
    } catch (e: Exception) {
        Log.d(TAG, "performNetworkCall threw", e)
        e.printStackTrace()
        NetworkResponse.Failure(e)
    }
}

/**
 * Retries the network call up to [maxRetries] times, returning the first success or the last failure
 * if no success was accomplished.
 */
suspend inline fun <T> retryNetworkCall(
    maxRetries: Int = 3,
    delayBetweenRetries: Duration = Duration.ZERO,
    crossinline performNetworkCall: suspend () -> NetworkResponse<T>,
): NetworkResponse<T> {
    var tryNum = 0
    while (true) {
        if (tryNum != 0) delay(delayBetweenRetries)

        return when (val response = performNetworkCall()) {
            is NetworkResponse.Failure -> if (tryNum >= maxRetries - 1) {
                response
            } else {
                tryNum++
                continue
            }

            is NetworkResponse.Response -> response
        }
    }
}