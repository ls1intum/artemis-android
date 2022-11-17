package de.tum.informatics.www1.artemis.native_app.feature.course_view

import kotlinx.datetime.LocalDate

/**
 * Exercises grouped by a single week
 */
internal sealed class WeeklyExercises(val exercises: List<ExerciseWithParticipationStatus>) {
    class BoundToWeek(
        val firstDayOfWeek: LocalDate,
        val lastDayOfWeek: LocalDate,
        exercises: List<ExerciseWithParticipationStatus>
    ) : WeeklyExercises(exercises)

    class Unbound(
        exercises: List<ExerciseWithParticipationStatus>
    ) : WeeklyExercises(exercises)
}