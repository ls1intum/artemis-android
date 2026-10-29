package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import kotlinx.serialization.Serializable

/**
 * The exercises of a course, as answered by "api/course/courses/{courseId}/exercises-for-overview".
 *
 * The response also carries the requesting user's scores for the course; no screen reads them here,
 * so they are left out. The dashboard gets its scores from "courses/for-dashboard" instead.
 *
 * The exercises are projected to what an overview renders, so fields such as the problem statement
 * are absent; the exercise detail endpoint carries them.
 */
@Serializable
data class CourseExercisesForOverview(
    val exercises: List<Exercise> = emptyList(),
)
