package de.tum.informatics.www1.artemis.native_app.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun Instant.hasPassedFlow(): Flow<Boolean> = flow {
    val time = this@hasPassedFlow
    val now = Clock.System.now()
    emit(time < now)

    if (time > now) {
        delay(time - now)
        emit(true)
    }
}

fun Instant.isInFutureFlow(): Flow<Boolean> = hasPassedFlow().map { !it }