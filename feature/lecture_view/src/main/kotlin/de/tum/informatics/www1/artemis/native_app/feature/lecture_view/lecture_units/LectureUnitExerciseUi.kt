package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

@Composable
internal fun LectureUnitExerciseUi(
    modifier: Modifier,
    lectureUnit: LectureUnitExercise,
    onClickExercise: (exerciseId: Long) -> Unit
) {
    val exercise = lectureUnit.exercise
    if (exercise != null) {
        ExerciseListItem(
            modifier = modifier,
            exercise = exercise,
            onClickExercise = { onClickExercise(exercise.id ?: return@ExerciseListItem) }
        )
    }
}