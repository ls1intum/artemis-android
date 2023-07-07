package de.tum.informatics.www1.artemis.native_app.core.data.test

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val TAG = "DataStateTestUtil"

fun <T> Flow<DataState<T>>.awaitFirstSuccess(message: String = "No message given", timeout: Duration = 1.seconds): T = runBlocking {
    try {
        withTimeout(timeout) {
            this@awaitFirstSuccess.filterSuccess().first()
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Did not receive data state success in $timeout: $message")
        throw e
    }
}