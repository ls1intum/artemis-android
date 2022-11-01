package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import kotlinx.datetime.LocalDate

/**
 * Exercises grouped by a single week
 */
sealed class WeeklyExercises(val exercises: List<ExerciseWithParticipationStatus>) {
    class BoundToWeek(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        exercises: List<ExerciseWithParticipationStatus>
    ) : WeeklyExercises(exercises)

    class Unbound(
        exercises: List<ExerciseWithParticipationStatus>
    ) : WeeklyExercises(exercises)
}