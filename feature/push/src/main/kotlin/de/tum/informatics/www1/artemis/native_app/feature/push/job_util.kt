package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

inline fun <reified T : ListenableWorker> defaultInternetWorkRequest(
    inputData: Data,
    configure: OneTimeWorkRequest.Builder.() -> Unit = {}
): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<T>()
        // Only run when the device is connected to the internet.
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .setInputData(inputData)
        .apply(configure)
        .build()
}