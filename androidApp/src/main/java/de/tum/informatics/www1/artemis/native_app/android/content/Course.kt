package de.tum.informatics.www1.artemis.native_app.android.content

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.Lecture
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representation of a single course.
 */
@Parcelize
@Serializable
data class Course(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("courseIcon") val courseIconPath: String,
    val exercises: List<Exercise> = emptyList(),
    val lectures: List<Lecture> = emptyList()
) : Parcelable {
    private val maxPointsPossibleHalves get() = exercises.sumOf { it.maxPointsHalves }
    private val currentScoreHalves get() = exercises.sumOf { it.currentScoreHalves }

    val progress: Float get() = currentScoreHalves.toFloat() / maxPointsPossibleHalves.toFloat()

    val maxPointsPossible: Float get() = maxPointsPossibleHalves.toFloat() / 2f
    val currentScore: Float get() = (currentScoreHalves.toFloat() / 2f)
}