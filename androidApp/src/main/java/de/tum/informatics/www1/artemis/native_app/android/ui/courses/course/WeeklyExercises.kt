package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import kotlinx.datetime.LocalDate

/**
 * Exercises grouped by a single week
 */
sealed class WeeklyExercises(val exercises: List<Exercise>) {
    class BoundToWeek(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        exercises: List<Exercise>
    ) : WeeklyExercises(exercises)

    class Unbound(
        exercises: List<Exercise>
    ) : WeeklyExercises(exercises)
}