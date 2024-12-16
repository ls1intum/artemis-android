package de.tum.informatics.www1.artemis.native_app.core.ui.date

import android.icu.text.SimpleDateFormat
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.util.Date

fun converDateAndTime(date: Instant?): String {
    return SimpleDateFormat.getDateTimeInstance(
        SimpleDateFormat.SHORT,
        SimpleDateFormat.SHORT
    ).format(Date.from(date?.toJavaInstant()))
}