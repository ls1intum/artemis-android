package de.tum.informatics.www1.artemis.native_app.android.model

import de.tum.informatics.www1.artemis.native_app.android.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.model.lecture.Lecture
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representation of a single course.
 */
@Serializable
data class Course(
    val id: Int,
    val title: String = "",
    val description: String = "",
    @SerialName("courseIcon") val courseIconPath: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val lectures: List<Lecture> = emptyList(),
    val semester: String = "",
    val registrationConfirmationMessage: String = ""
) {
    private val maxPointsPossibleHalves get() = exercises.sumOf { it.maxPointsHalves }
    private val currentScoreHalves get() = exercises.sumOf { it.currentScoreHalves }

    val progress: Float get() = currentScoreHalves.toFloat() / maxPointsPossibleHalves.toFloat()

    val maxPointsPossible: Float get() = maxPointsPossibleHalves.toFloat() / 2f
    val currentScore: Float get() = (currentScoreHalves.toFloat() / 2f)
}