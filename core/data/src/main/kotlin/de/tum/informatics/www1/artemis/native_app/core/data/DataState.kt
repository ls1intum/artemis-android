package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

/**
 * The data state of the request.
 */
sealed class DataState<T> {
    /**
     * Currently loading.
     */
    data class Loading<T>(val isSuspended: Boolean = false) : DataState<T>()

    class Failure<T>(val throwable: Throwable) : DataState<T>()

    data class Success<T>(val data: T) : DataState<T>()

    inline fun <K> bind(op: (T) -> K): DataState<K> {
        return when (this) {
            is Success -> Success(op(data))
            is Failure -> Failure(throwable)
            is Loading -> Loading()
        }
    }

    inline fun <K> transform(op: (T) -> DataState<K>): DataState<K> {
        return when (this) {
            is Success -> op(data)
            is Failure -> Failure(throwable)
            is Loading -> Loading()
        }
    }

    fun orElse(other: T): T {
        return when (this) {
            is Success -> data
            else -> other
        }
    }

    fun orThrow(): T {
        return when (this) {
            is Success -> data
            else -> throw IllegalStateException("Data state is $this but Success was expected")
        }
    }
}

fun <T> DataState<T>.orNull(): T? {
    return when (this) {
        is DataState.Success -> data
        else -> null
    }
}

/**
 * @return a data state that is only a success if both datastates are a success
 */
infix fun <T, K> DataState<T>.join(other: DataState<K>): DataState<Pair<T, K>> {
    return when {
        this is DataState.Success && other is DataState.Success -> DataState.Success(this.data to other.data)
        this is DataState.Failure && other is DataState.Failure -> DataState.Failure(this.throwable)
        this is DataState.Failure -> DataState.Failure(this.throwable)
        other is DataState.Failure -> DataState.Failure(other.throwable)
        this is DataState.Loading || other is DataState.Loading -> DataState.Loading()
        else -> DataState.Failure(IllegalStateException())
    }
}

fun <T, K, L> DataState<T>.join(first: DataState<K>, second: DataState<L>): DataState<Triple<T, K, L>> {
    return (this join first join second).bind { (ab, c) ->
        val (a, b) = ab
        Triple(a, b, c)
    }
}

val <T> DataState<T>.isSuccess: Boolean
    get() = when (this) {
        is DataState.Success<T> -> true
        else -> false
    }

fun <T> Flow<DataState<T>>.filterSuccess(): Flow<T> = transform {
    when (it) {
        is DataState.Success<T> -> emit(it.data)
        else -> {}
    }
}

fun <T> Flow<DataState<T>>.keepSuccess(): Flow<DataState<T>> = filter { it.isSuccess }

fun <T> Flow<DataState<T>>.stateIn(scope: CoroutineScope, sharingStarted: SharingStarted): StateFlow<DataState<T>> =
    stateIn(scope, sharingStarted, DataState.Loading())

/**
 * Retries the given network call up to [maxRetries] times, after each attempt pausing using an exponential backoff
 * strategy, with [baseBackoffMillis] as initial parameter. If the connectivity state changes, the
 * whole function is executed again from scratch. Once the call was successful, no more elements will be emitted
 */
inline fun <T> retryOnInternet(
    connectivity: Flow<NetworkStatusProvider.NetworkStatus>,
    baseBackoffMillis: Long = 200,
    minimumLoadingMillis: Long = 0,
    maxRetries: Int = 3,
    crossinline perform: suspend () -> NetworkResponse<T>
): Flow<DataState<T>> {
    check(maxRetries > 0)

    return connectivity
        .transformLatest {
            emit(DataState.Loading())

            var currentBackoff = baseBackoffMillis

            val start = System.currentTimeMillis()
            for (i in 0 until maxRetries) {
                when (val result = perform()) {
                    is NetworkResponse.Response -> {
                        val end = Clock.System.now().toEpochMilliseconds()
                        val remainingWaitTime = minimumLoadingMillis - (end - start)
                        if (remainingWaitTime > 0) delay(remainingWaitTime)

                        emit(DataState.Success(result.data))
                        return@transformLatest
                    }

                    is NetworkResponse.Failure -> {
                        if (i == maxRetries - 1) {
                            // Failure
                            emit(DataState.Failure(result.exception))
                        }
                    }
                }

                delay(currentBackoff)
                currentBackoff += 2
            }
        }
        .catch {  error ->
            emit(DataState.Failure(error))
        }
        .transformWhile { dataState ->
            emit(dataState)
            // Continue while the network response is still a failure.
            dataState !is DataState.Success
        }
}

/**
 * Performs the given network response once an internet connection is available.
 * If the request fails, it is retried using an exponential backoff approach, however, only if internet is available.
 * The exponential backoff timer is reset if connection is lost.
 * Once the first request succeeded, nothing is emitted anymore.
 *
 * The first element emitted is always [DataState.Suspended]
 */
inline fun <T> retryOnInternetIndefinetly(
    connectivity: Flow<NetworkStatusProvider.NetworkStatus>,
    baseBackoffMillis: Long = 2000,
    crossinline perform: suspend () -> NetworkResponse<T>
): Flow<DataState<T>> {
    return connectivity
        // We simply restart the exponential backoff when the connectivity has changed.
        .transformLatest { _ ->
            //Fetch data with exponential backoff
            var currentBackoff = baseBackoffMillis

            while (true) {
                emit(DataState.Loading())
                when (val response = perform()) {
                    is NetworkResponse.Response -> {
                        emit(DataState.Success(response.data))
                        return@transformLatest
                    }

                    is NetworkResponse.Failure -> {
                        emit(DataState.Failure(response.exception))
                    }
                }

                //Perform exponential backoff
                delay(currentBackoff)
                currentBackoff *= 2
            }
        }
        .catch { error -> emit(DataState.Failure(error)) }
        .transformWhile { dataState ->
            emit(dataState)
            // Continue while the network response is still a failure.
            dataState !is DataState.Success
        }
        .onStart { emit(DataState.Loading()) }
}