package de.tum.informatics.www1.artemis.native_app.core.ui.date

import android.icu.text.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Format the given time relative to now and update as often as relevant.
 * @param formatSeconds if second precision should be used or minute precision.
 */
@Composable
fun getRelativeTime(
    to: Instant,
    clock: Clock = Clock.System,
    formatSeconds: Boolean = false,
    showDate: Boolean = true,
    showDateAndTime: Boolean = false
): CharSequence {
    val isShowDateParameterCombinationIllegal = showDateAndTime && !showDate
    require(!isShowDateParameterCombinationIllegal)

    val timeDifferenceBelowOneMinuteString =
        stringResource(id = R.string.time_difference_under_one_minute)

    val flow = remember(to, clock, timeDifferenceBelowOneMinuteString) {
        flow {
            while (true) {
                val now = clock.now()

                val timeDifference = (now - to).absoluteValue

                if (timeDifference >= 1.days && !showDate) {
                    emit(
                        to.format(DateFormats.OnlyTime.format)
                    )
                } else if (timeDifference >= 1.days && showDateAndTime) {
                    emit(
                        to.format(DateFormats.DefaultDateAndTime.format)
                    )
                } else if (formatSeconds || timeDifference >= 1.minutes) {
                    emit(
                        DateUtils.getRelativeTimeSpanString(
                            to.toEpochMilliseconds(),
                            now.toEpochMilliseconds(),
                            0L,
                            DateUtils.FORMAT_ABBREV_RELATIVE
                        )
                    )
                } else {
                    emit(timeDifferenceBelowOneMinuteString)
                }

                when {
                    timeDifference < 1.minutes && formatSeconds -> {
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
    }

    return flow.collectAsState(initial = "").value
}

fun Instant.format(f: DateFormat) = f.format(Date.from(this.toJavaInstant()))