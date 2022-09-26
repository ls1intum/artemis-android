package de.tum.informatics.www1.artemis.native_app.android.util

/**
 * The data state of the request, either loading or the call is done.
 */
sealed class DataState<T> {
    class Loading<T> : DataState<T>()

    data class Done<T>(val response: NetworkResponse<T>) : DataState<T>()
}