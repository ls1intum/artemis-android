package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class CourseWithScore(
    val course: Course,
    val totalScores: TotalScores
) {
    @Serializable
    data class TotalScores(
        val maxPoints: Float,
        val reachablePoints: Float,
        val studentScores: StudentScores
    ) {
        @Serializable
        data class StudentScores(
            val absoluteScore: Float,
            val relativeScore: Float,
            val currentRelativeScore: Float,
            val presentationScore: Float
        )
    }
}

fun CourseWithScore.upcomingExercises(): List<Exercise> {
    return course.exercises
        .filter { exercise ->
            val dueDate = exercise.dueDate
            dueDate == null || dueDate > Clock.System.now()
        }
        .sortedWith { exerciseA, exerciseB ->
            val dueDateA = exerciseA.dueDate
            val dueDateB = exerciseB.dueDate
            when {
                dueDateA != null && dueDateB != null -> dueDateA.compareTo(dueDateB)
                dueDateA == null -> 1
                else -> -1
            }
        }
}