package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitJobCompletion
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

@Composable
fun AnimatedFloatingActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = enabled,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        FloatingActionButton(
            onClick = onClick,
            content = content
        )
    }
}

/**
 * A floating action button that will launch a job when pressed and keep track of the progress of the job.
 */
@Composable
fun <T> JobAnimatedFloatingActionButton(
    enabled: Boolean,
    startJob: () -> Deferred<T>,
    onJobCompleted: (T) -> Unit,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var job: Deferred<T>? by remember { mutableStateOf(null) }

    AwaitDeferredCompletion(job) { value ->
        job = null
        onJobCompleted(value)
    }

    AnimatedFloatingActionButton(
        enabled = enabled,
        onClick = {
            job?.cancel()
            job = startJob()

            onClick()
        }
    ) {
        if (job == null) {
            content()
        } else {
            CircularProgressIndicator()
        }
    }
}