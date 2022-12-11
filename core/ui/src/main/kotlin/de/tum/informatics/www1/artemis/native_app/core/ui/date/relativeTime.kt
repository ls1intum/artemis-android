package de.tum.informatics.www1.artemis.native_app.core.ui.date

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import androidx.compose.runtime.collectAsState
import okhttp3.internal.wait
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Format the given time relative to now and update as often as relevant.
 */
@Composable
fun getRelativeTime(to: Instant): CharSequence {
    val flow = flow {
        while (true) {
            val now = Clock.System.now()
            emit(
                DateUtils.getRelativeTimeSpanString(
                    to.toEpochMilliseconds(),
                    now.toEpochMilliseconds(),
                    0L,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
            )

            val timeDifference = now - to
            when {
                timeDifference < 1.minutes -> {
                    delay(1.seconds)
                }

                timeDifference < 1.hours -> {
                    // Wait until the next minute
                    val passedMinutes = timeDifference.inWholeMinutes
                    val waitUntil = to + (passedMinutes + 1).minutes
                    delay(waitUntil - now)
                }

                else -> {
                    // update every hour
                    val passedHours = timeDifference.inWholeHours
                    val waitUntil = to + (passedHours + 1).hours
                    delay(waitUntil - now)
                }
            }
        }
    }

    return flow.collectAsState(initial = "").value
}