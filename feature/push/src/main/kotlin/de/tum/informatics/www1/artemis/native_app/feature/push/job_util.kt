package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.work.*
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
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .setInputData(inputData)
        .apply(configure)
        .build()
}