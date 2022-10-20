package de.tum.informatics.www1.artemis.native_app.util

/**
 * Wrapper around network responses. Use to propagate failures correctly.
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

/**
 * Perform a network call, returning a wrapper with the response.
 * If it fails, a failure object is returned instead.
 */
suspend inline fun <T> performNetworkCall(crossinline perform: suspend () -> T): NetworkResponse<T> {
    return try {
        NetworkResponse.Response(perform())
    } catch (e: Exception) {
        e.printStackTrace()
        NetworkResponse.Failure(e)
    }
}