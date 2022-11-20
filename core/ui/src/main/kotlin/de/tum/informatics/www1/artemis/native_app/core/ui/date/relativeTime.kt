package de.tum.informatics.www1.artemis.native_app.core.ui.date

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import androidx.compose.runtime.collectAsState

/**
 * Format the given time relative to now and update every minute.
 */
@Composable
fun getRelativeTime(to: Instant): CharSequence {
    val flow = flow {
        while (true) {
            emit(
                DateUtils.getRelativeTimeSpanString(
                    to.toEpochMilliseconds(),
                    Clock.System.now().toEpochMilliseconds(),
                    0L,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
            )

            delay(1.minutes)
        }
    }

    return flow.collectAsState(initial = "").value
}