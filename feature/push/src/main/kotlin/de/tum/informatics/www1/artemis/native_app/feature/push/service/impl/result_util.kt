package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import androidx.work.ListenableWorker
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import io.ktor.http.*

fun NetworkResponse<HttpStatusCode>.toWorkerResult(): ListenableWorker.Result {
    return when (this) {
        is NetworkResponse.Failure -> ListenableWorker.Result.retry()
        is NetworkResponse.Response -> if (data.isSuccess()) {
            ListenableWorker.Result.success()
        } else ListenableWorker.Result.failure()
    }
}