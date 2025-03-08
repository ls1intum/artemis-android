package de.tum.informatics.www1.artemis.native_app.feature.courseview

import kotlinx.datetime.Clock

fun <T> List<T>.groupByTimeFrame(
    now: kotlinx.datetime.Instant = Clock.System.now(),
    getStartDate: (T) -> kotlinx.datetime.Instant?,
    getEndDate: (T) -> kotlinx.datetime.Instant?,
    isDueSoon: (T) -> Boolean = { false }
): List<TimeFrame<T>> {
    if (isEmpty()) return emptyList()

    val (past, notPast) = partition { item ->
        val start = getStartDate(item)
        val end = getEndDate(item)
        (start == null || start < now) && (end != null && end < now)
    }
    val (noDate, dated) = notPast.partition { item ->
        val start = getStartDate(item)
        val end = getEndDate(item)
        (start == null || start < now) && end == null
    }
    val (dueSoon, currentOrFuture) = dated.partition { item ->
        isDueSoon(item)
    }
    val (current, future) = currentOrFuture.partition { item ->
        val start = getStartDate(item)
        val end = getEndDate(item)
        val hasStarted = (start == null || start <= now)
        val notEnded = (end != null && end > now)
        hasStarted && notEnded
    }

    val grouped = mutableListOf<TimeFrame<T>>()

    if (past.isNotEmpty()) grouped += TimeFrame.Past(past)
    if (current.isNotEmpty()) grouped += TimeFrame.Current(current)
    if (dueSoon.isNotEmpty()) grouped += TimeFrame.DueSoon(dueSoon)
    if (future.isNotEmpty()) grouped += TimeFrame.Future(future)
    if (noDate.isNotEmpty()) grouped += TimeFrame.NoDate(noDate)

    return grouped
}
