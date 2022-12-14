package de.tum.informatics.www1.artemis.native_app.core.ui.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.common.hasPassedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun Instant.hasPassed(): Boolean {
    return remember(this) { this.hasPassedFlow() }
        .collectAsState(initial = this < Clock.System.now())
        .value
}

@Composable
fun Instant.isInFuture(): Boolean {
    return !hasPassed()
}