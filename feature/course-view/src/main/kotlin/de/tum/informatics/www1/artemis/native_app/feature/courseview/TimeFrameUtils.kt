package de.tum.informatics.www1.artemis.native_app.feature.courseview

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun <T> List<T>.groupByTimeFrame(
    now: Instant = Clock.System.now(),
    getStartDate: (T) -> Instant?,
    getEndDate: (T) -> Instant?,
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

    fun List<T>.sortedByStartDate() = this.sortedBy { getStartDate(it) ?: Instant.DISTANT_PAST }

    val pastSorted = past.sortedByStartDate()
    val currentSorted = current.sortedByStartDate()
    val dueSoonSorted = dueSoon.sortedByStartDate()
    val futureSorted = future.sortedByStartDate()
    val noDateSorted = noDate.sortedByStartDate()

    val grouped = buildList {
        if (pastSorted.isNotEmpty()) add(TimeFrame.Past(pastSorted))
        if (currentSorted.isNotEmpty()) add(TimeFrame.Current(currentSorted))
        if (dueSoonSorted.isNotEmpty()) add(TimeFrame.DueSoon(dueSoonSorted))
        if (futureSorted.isNotEmpty()) add(TimeFrame.Future(futureSorted))
        if (noDateSorted.isNotEmpty()) add(TimeFrame.NoDate(noDateSorted))
    }

    return grouped
}

