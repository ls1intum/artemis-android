package de.tum.informatics.www1.artemis.native_app.android.util

import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState.Suspended
import kotlinx.coroutines.flow.*

/**
 * The data state of the request.
 */
sealed class DataState<T> {
    /**
     * Waiting until a valid internet connection is available again.
     */
    class Suspended<T>(val exception: Exception? = null) : DataState<T>()

    /**
     * Currently loading.
     */
    class Loading<T> : DataState<T>()

    class Failure<T>(val exception: Exception) : DataState<T>()

    data class Success<T>(val data: T) : DataState<T>()
}

/**
 * Perform a network call, returning a wrapper with the response.
 * If it fails, a failure object is returned instead.
 */
inline fun <T> fetchData(
    crossinline produceFailureState: (Exception) -> DataState<T>,
    crossinline perform: suspend () -> T
): Flow<DataState<T>> {
    return flow {
        emit(DataState.Loading())

        try {
            emit(DataState.Success(perform()))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(produceFailureState(e))
        }
    }
}

/**
 * Performs the given network response once an internet connection is available.
 * If the request fails, it is retried once a connection to the internet is available again.
 * Once the first request succeeded, nothing is emitted anymore.
 *
 * The first element emitted is always [DataState.Suspended]
 */
inline fun <T> retryOnInternet(
    connectivity: Flow<NetworkStatusProvider.NetworkStatus>,
    crossinline perform: suspend () -> T
): Flow<DataState<T>> {
    return connectivity
        .filter { it == NetworkStatusProvider.NetworkStatus.Internet }
        .transformLatest {
            emitAll(
                fetchData(::Suspended, perform)
            )
        }
        .transformWhile { dataState ->
            emit(dataState)
            //Continue while the network response is still a failure.
            dataState !is DataState.Success
        }
        .onStart { emit(Suspended(null)) }
}