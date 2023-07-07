package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

@Composable
internal fun LectureUnitExerciseUi(
    modifier: Modifier,
    lectureUnit: LectureUnitExercise,
    onClickExercise: (exerciseId: Long) -> Unit,
    exerciseActions: BoundExerciseActions
) {
    val exercise = lectureUnit.exercise
    if (exercise != null) {
        ExerciseListItem(
            modifier = modifier,
            exercise = exercise,
            onClickExercise = { onClickExercise(exercise.id ?: 0L) },
            exerciseActions = remember(exerciseActions, exercise) {
                exerciseActions.getUnbound(
                    exerciseId = exercise.id ?: 0L
                )
            }
        )
    }
}