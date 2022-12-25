package de.tum.informatics.www1.artemis.native_app.core.common

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

data class ClockWithOffset internal constructor(private val source: Clock, val delta: Duration) : Clock {
    override fun now(): Instant {
        return source.now().plus(delta)
    }
}

/**
 * Construct a new clock which is offset by the given clock.
 * If the receiver is a clock with offset, the durations are added and the source clock is the system clock.
 */
fun Clock.offsetBy(duration: Duration): ClockWithOffset {
    return if (this is ClockWithOffset) {
        // Do not chain clock with offset, simply add the durations
        ClockWithOffset(Clock.System, this.delta + duration)
    } else {
        ClockWithOffset(this, duration)
    }
}