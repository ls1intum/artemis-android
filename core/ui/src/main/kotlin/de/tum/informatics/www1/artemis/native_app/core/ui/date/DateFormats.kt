package de.tum.informatics.www1.artemis.native_app.core.ui.date

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat

enum class DateFormats(val format: DateFormat) {
    DefaultDateAndTime(SimpleDateFormat.getDateTimeInstance(
        SimpleDateFormat.MEDIUM,
        SimpleDateFormat.SHORT
    )),
    OnlyTime(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)),
    EditTimestamp(SimpleDateFormat.getDateTimeInstance(
        SimpleDateFormat.SHORT,
        SimpleDateFormat.SHORT
    )),
    OnlyDate(SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG)),
}
