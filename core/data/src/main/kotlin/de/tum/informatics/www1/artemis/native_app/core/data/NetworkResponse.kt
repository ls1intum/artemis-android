package de.tum.informatics.www1.artemis.native_app.core.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Wrapper around network responses. Used to propagate failures correctly.
 */
sealed class NetworkResponse<T> {
    inline fun <K> bind(transform: (T) -> K): NetworkResponse<K> {
        return when (this) {
            is Failure -> Failure(exception)
            is Response -> Response(transform(data))
        }
    }

    inline fun <K> map(mapSuccess: (T) -> K, mapFailure: (Exception) -> K): K = when(this) {
        is Failure -> mapFailure(exception)
        is Response -> mapSuccess(data)
    }

    inline fun <K> then(transform: (T) -> NetworkResponse<K>): NetworkResponse<K> {
        return when (this) {
            is Response -> transform(data)
            is Failure -> Failure(exception)
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

    fun orThrow(message: String = ""): T = when (this) {
        is Response -> data
        is Failure -> throw RuntimeException("Network Response does not contain response: $message; exception = $exception")
    }

    data class Response<T>(val data: T) : NetworkResponse<T>()

    data class Failure<T>(val exception: Exception) : NetworkResponse<T>()
}

const val TAG = "NetworkResponse"

suspend inline fun <T> performNetworkCall(
    crossinline perform: suspend () -> T
): NetworkResponse<T> {
    return try {
        NetworkResponse.Response(perform())
//        withTimeout(10.seconds) {
//        }
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

inline fun <T> NetworkResponse<T>.onSuccess(onSuccess: (T) -> Unit): NetworkResponse<T> {
    if (this is NetworkResponse.Response) {
        onSuccess(data)
    }

    return this
}

inline fun <T> NetworkResponse<T>.onFailure(onFailure: (Throwable) -> Unit): NetworkResponse<T> {
    if (this is NetworkResponse.Failure) {
        onFailure(exception)
    }

    return this
}