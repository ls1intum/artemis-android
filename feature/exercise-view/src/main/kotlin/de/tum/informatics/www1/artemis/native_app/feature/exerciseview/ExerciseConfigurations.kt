package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ExerciseConfiguration(
    val navigationLevel: Int,
    val prev: ExerciseConfiguration?
) : Parcelable

@Parcelize
object NothingOpened : ExerciseConfiguration(0, null)

@Parcelize
data class OpenedExercise(
    private val _prev: ExerciseConfiguration,
    val exerciseId: Long
) : ExerciseConfiguration(10, _prev)

fun getInitialExerciseConfigurations(
    exerciseId: Long?
): ExerciseConfiguration = when {
    exerciseId != null ->  OpenedExercise(
        exerciseId = exerciseId,
        _prev = NothingOpened
    )

    else -> NothingOpened
}