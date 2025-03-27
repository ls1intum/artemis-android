package de.tum.informatics.www1.artemis.native_app.feature.courseview

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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


fun <T> groupByWeek(
    items: List<T>,
): List<WeeklyGroup<T>> {
    if (items.isEmpty()) return emptyList()

    val map = mutableMapOf<Pair<Int?, Int?>, MutableList<T>>()
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    for (item in items) {

        val end = when (item) {
            is Exercise -> item.dueDate
            is Lecture -> item.endDate
            else -> null
        }
        if (end == null) {
            map.getOrPut(null to null) { mutableListOf() }.add(item)
        } else {
            val cal = Calendar.getInstance()
            cal.timeInMillis = end.toEpochMilliseconds()
            val year = cal.get(Calendar.YEAR)
            val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
            map.getOrPut(year to weekOfYear) { mutableListOf() }.add(item)
        }
    }

    return map
        .toList()
        .sortedBy { (yearWeek, _) ->
            val (year, week) = yearWeek
            if (year == null || week == null) Instant.DISTANT_FUTURE
            else {
                val cal = Calendar.getInstance()
                cal.clear()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.minimalDaysInFirstWeek = 4
                cal.setWeekDate(year, week, Calendar.MONDAY)
                Instant.fromEpochMilliseconds(cal.timeInMillis)
            }
        }
        .map { (yearWeek, bucketItems) ->
            val (year, week) = yearWeek
            val label = if (year == null || week == null) {
                "No Date Associated"
            } else {
                val cal = Calendar.getInstance()
                cal.clear()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.minimalDaysInFirstWeek = 4
                cal.setWeekDate(year, week, Calendar.MONDAY)
                val start = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val end = cal.time
                "${fmt.format(start)} - ${fmt.format(end)}"
            }
            WeeklyGroup(label, bucketItems)
        }
}