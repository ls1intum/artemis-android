package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeFrameUtils {
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

        fun List<T>.sortedByStartDate() = this.sortedByDescending { getStartDate(it) ?: Instant.DISTANT_FUTURE }

        val pastSorted = past.sortedByStartDate()
        val currentSorted = current.sortedByStartDate()
        val dueSoonSorted = dueSoon.sortedByStartDate()
        val futureSorted = future.sortedByStartDate()
        val noDateSorted = noDate.sortedByStartDate()

        val grouped = buildList {
            if (futureSorted.isNotEmpty()) add(TimeFrame.Future(futureSorted))
            if (dueSoonSorted.isNotEmpty()) add(TimeFrame.DueSoon(dueSoonSorted))
            if (currentSorted.isNotEmpty()) add(TimeFrame.Current(currentSorted))
            if (pastSorted.isNotEmpty()) add(TimeFrame.Past(pastSorted))
            if (noDateSorted.isNotEmpty()) add(TimeFrame.NoDate(noDateSorted))
        }

        return grouped
    }

    fun <T> groupByWeek(items: List<T>): List<WeeklyGroup<T>> {
        if (items.isEmpty()) return emptyList()

        val map = mutableMapOf<WeekIndicator, MutableList<T>>()

        for (item in items) {
            val end = when (item) {
                is Exercise -> item.dueDate
                is Lecture -> item.endDate ?: item.startDate
                else -> null
            }
            if (end == null) {
                map.getOrPut(WeekIndicator.NoDate) { mutableListOf() }.add(item)
            } else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = end.toEpochMilliseconds()

                val indicator = WeekIndicator.Dated(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.WEEK_OF_YEAR)
                )

                map.getOrPut(indicator) { mutableListOf() }.add(item)
            }
        }

        return map
            .map { (indicator, bucketItems) -> WeeklyGroup(indicator, bucketItems) }
            .sortedByDescending { (indicator, _) ->
                toSortInstant(indicator)
            }
    }

    private fun Calendar.configureIsoWeek(year: Int, weekOfYear: Int) {
        clear()
        firstDayOfWeek = Calendar.MONDAY
        minimalDaysInFirstWeek = 4
        setWeekDate(year, weekOfYear, Calendar.MONDAY)
    }

    private fun computeWeekStartDate(indicator: WeekIndicator): Date? {
        return when (indicator) {
            is WeekIndicator.Dated -> {
                val cal = Calendar.getInstance()
                cal.configureIsoWeek(indicator.year, indicator.weekOfYear)
                cal.time
            }
            WeekIndicator.NoDate -> null
        }
    }

    private fun toSortInstant(indicator: WeekIndicator): Instant {
        val startDate = computeWeekStartDate(indicator)
            ?: return Instant.DISTANT_FUTURE
        return Instant.fromEpochMilliseconds(startDate.time)
    }

    @Composable
    fun WeekIndicator.toRangeString(): String {
        return when (this) {
            is WeekIndicator.Dated -> {
                val start = computeWeekStartDate(this) ?: return stringResource(R.string.course_ui_sub_title_no_date)
                val cal = Calendar.getInstance().apply { time = start }
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val end = cal.time

                val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "${fmt.format(start)} - ${fmt.format(end)}"
            }

            WeekIndicator.NoDate -> {
                stringResource(R.string.course_ui_sub_title_no_date)
            }
        }
    }

}