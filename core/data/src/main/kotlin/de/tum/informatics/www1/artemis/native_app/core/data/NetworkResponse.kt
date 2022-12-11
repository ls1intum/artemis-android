package de.tum.informatics.www1.artemis.native_app.core.data

import android.util.Log
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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
    } catch (e: Exception) {
        Log.d(TAG, "performNetworkCall threw", e)
        NetworkResponse.Failure(e)
    }
}