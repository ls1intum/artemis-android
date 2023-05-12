package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

/**
 * If job is not null, waits for the job to complete and calls [onComplete] upon completion of [job].
 */
@Composable
fun AwaitJobCompletion(job: Job?, onComplete: () -> Unit) {
    LaunchedEffect(job) {
        job?.let {
            job.join()
            onComplete()
        }
    }
}

@Composable
fun <T> AwaitDeferredCompletion(job: Deferred<T>?, onComplete: (T) -> Unit) {
    LaunchedEffect(job) {
        job?.let {
            onComplete(job.await())
        }
    }
}