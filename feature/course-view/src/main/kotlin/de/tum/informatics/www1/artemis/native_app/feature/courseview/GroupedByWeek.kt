package de.tum.informatics.www1.artemis.native_app.feature.courseview

import kotlinx.datetime.LocalDate

/**
 * Data points grouped by a single week
 */
internal sealed class GroupedByWeek<T>(val items: List<T>) {

    /**
     * Key which can be used to to identify the instance in a map
     */
    abstract val key: String

    class BoundToWeek<T>(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        items: List<T>
    ) : GroupedByWeek<T>(items) {
        override val key: String
            get() = firstDayOfWeek.toString()
    }

    class Unbound<T>(
        items: List<T>
    ) : GroupedByWeek<T>(items) {
        override val key: String
            get() = "Unbound"
    }
}