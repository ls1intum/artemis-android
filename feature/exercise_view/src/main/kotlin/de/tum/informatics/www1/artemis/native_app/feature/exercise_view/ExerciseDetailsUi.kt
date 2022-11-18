package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon

@Composable
internal fun ExerciseDetailsUi(
    modifier: Modifier,
    exercise: Exercise,
    latestResult: Result?,
    hasMultipleResults: Boolean
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExerciseCategoryChipRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise
        )


        ExerciseDetails(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise,
            latestResult = latestResult
        )
    }
}

/**
 * Display the exercise info.
 * Includes: Max points reachable, the assessment type and submission status.
 */
@Composable
private fun ExerciseDetails(modifier: Modifier, exercise: Exercise, latestResult: Result?) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(id = R.string.exercise_info_title),
                style = MaterialTheme.typography.titleSmall
            )

            val detailsComponentModifier = Modifier.fillMaxWidth()
            val dividerModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)

            ExerciseDetailsComponent(
                modifier = detailsComponentModifier,
                title = stringResource(id = R.string.exercise_info_exercise_type)
            ) {
                Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = getExerciseTypeIcon(exercise), contentDescription = null)

                    Text(text = getExerciseReadableName(exercise))
                }
            }

            Divider(modifier = dividerModifier)

            val points = exercise.maxPoints
            val currentUserPoints = latestResult?.score

            if (points != null && currentUserPoints == null) {
                ExerciseDetailsTextComponent(
                    modifier = detailsComponentModifier,
                    title = stringResource(id = R.string.exercise_info_points),
                    value = points.toString()
                )

                Divider(modifier = dividerModifier)
            } else if (points != null && currentUserPoints != null) {
                ExerciseDetailsTextComponent(
                    modifier = detailsComponentModifier,
                    title = stringResource(id = R.string.exercise_info_reached_points),
                    value = stringResource(
                        id = R.string.exercise_info_reached_points_value,
                        currentUserPoints,
                        points
                    )
                )

                Divider(modifier = dividerModifier)
            }

            val assessmentType = exercise.assessmentType
            if (assessmentType != null) {
                ExerciseDetailsTextComponent(
                    modifier = detailsComponentModifier,
                    title = stringResource(id = R.string.exercise_info_assessment),
                    value = getAssessmentTypeReadableName(assessmentType)
                )
            }

            if (exercise.getDueDate(exercise.getRes))
        }
    }
}

@Composable
private fun ExerciseDetailsTextComponent(modifier: Modifier, title: String, value: String) {
    ExerciseDetailsComponent(
        modifier = modifier,
        title = title,
        content = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
private fun ExerciseDetailsComponent(
    modifier: Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )

        content()
    }
}


/**
 * @return human readable name for the type of exercise.
 */
@Composable
private fun getExerciseReadableName(exercise: Exercise): String {
    return stringResource(
        id = when (exercise) {
            is FileUploadExercise -> R.string.exercise_type_file_upload
            is ModelingExercise -> R.string.exercise_type_modeling
            is ProgrammingExercise -> R.string.exercise_type_programming
            is QuizExercise -> R.string.exercise_type_quiz
            is TextExercise -> R.string.exercise_type_text
            is UnknownExercise -> R.string.exercise_type_unknown
        }
    )
}

@Composable
private fun getAssessmentTypeReadableName(assessmentType: Exercise.AssessmentType): String {
    return stringResource(
        id = when (assessmentType) {
            Exercise.AssessmentType.AUTOMATIC -> R.string.assessment_type_automatic
            Exercise.AssessmentType.SEMI_AUTOMATIC -> R.string.assessment_type_semi_automatic
            Exercise.AssessmentType.MANUAL -> R.string.assessment_type_manual
        }
    )
}