package de.tum.informatics.www1.artemis.native_app.core.model

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