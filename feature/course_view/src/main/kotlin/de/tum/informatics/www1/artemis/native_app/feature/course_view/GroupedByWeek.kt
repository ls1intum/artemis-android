package de.tum.informatics.www1.artemis.native_app.feature.course_view

import kotlinx.datetime.LocalDate

/**
 * Exercises grouped by a single week
 */
internal sealed class GroupedByWeek<T>(val items: List<T>) {
    class BoundToWeek<T>(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        items: List<T>
    ) : GroupedByWeek<T>(items)

    class Unbound<T>(
        items: List<T>
    ) : GroupedByWeek<T>(items)
}