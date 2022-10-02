package de.tum.informatics.www1.artemis.native_app.android.util

import android.net.Network
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import kotlinx.coroutines.flow.*

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