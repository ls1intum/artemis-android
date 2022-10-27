package de.tum.informatics.www1.artemis.native_app.android.util

import android.provider.ContactsContract.Data
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState.Failure
import de.tum.informatics.www1.artemis.native_app.android.util.DataState.Suspended
import kotlinx.coroutines.delay
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

    fun <K> bind(op: (T) -> K): DataState<K> {
        return when (this) {
            is Success -> Success(op(data))
            is Failure -> Failure(exception)
            is Loading -> Loading()
            is Suspended -> Suspended(exception)
        }
    }

    fun orElse(other: T): T {
        return when (this) {
            is Success -> data
            else -> other
        }
    }
}

val <T> DataState<T>.isSuccess: Boolean
    get() = when (this) {
        is DataState.Success<T> -> true
        else -> false
    }

/**
 * Performs the given network response once an internet connection is available.
 * If the request fails, it is retried using an exponential backoff approach, however, only if internet is available.
 * The exponential backoff timer is reset if connection is lost.
 * Once the first request succeeded, nothing is emitted anymore.
 *
 * The first element emitted is always [DataState.Suspended]
 */
inline fun <T> retryOnInternet(
    connectivity: Flow<NetworkStatusProvider.NetworkStatus>,
    baseBackoffMillis: Long = 2000,
    minimumLoadingMillis: Long = 0,
    crossinline perform: suspend () -> NetworkResponse<T>
): Flow<DataState<T>> {
    return connectivity
        .transformLatest { networkStatus ->
            when (networkStatus) {
                NetworkStatusProvider.NetworkStatus.Internet -> {
                    //Fetch data with exponential backoff

                    var currentBackoff = baseBackoffMillis

                    while (true) {
                        emit(DataState.Loading())

                        val start = System.currentTimeMillis()
                        when (val response = perform()) {
                            is NetworkResponse.Response -> {
                                val end = System.currentTimeMillis()
                                val remainingWaitTime = minimumLoadingMillis - (end - start)
                                if (remainingWaitTime > 0) delay(remainingWaitTime)

                                emit(DataState.Success(response.data))
                                return@transformLatest
                            }
                            is NetworkResponse.Failure -> {
                                emit(Failure(response.exception))
                            }
                        }

                        //Perform exponential backoff
                        delay(currentBackoff)
                        currentBackoff *= 2
                    }
                }
                NetworkStatusProvider.NetworkStatus.Unavailable -> {
                    //Just emit that this is suspended for now.
                    emit(Suspended())
                }
            }
        }
        .transformWhile { dataState ->
            emit(dataState)
            //Continue while the network response is still a failure.
            dataState !is DataState.Success
        }
        .onStart { emit(DataState.Loading()) }
}