package de.tum.informatics.www1.artemis.native_app.feature.course_view

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import kotlinx.datetime.LocalDate

/**
 * Exercises grouped by a single week
 */
internal sealed class WeeklyExercises(val exercises: List<Exercise>) {
    class BoundToWeek(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        exercises: List<Exercise>
    ) : WeeklyExercises(exercises)

    class Unbound(
        exercises: List<Exercise>
    ) : WeeklyExercises(exercises)
}