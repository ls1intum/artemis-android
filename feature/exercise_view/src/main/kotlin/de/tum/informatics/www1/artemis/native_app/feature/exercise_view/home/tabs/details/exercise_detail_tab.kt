package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import kotlinx.datetime.Instant
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

/**
 * Display the details of the exercise such as the points.
 */
@Composable
internal fun ExerciseDetailsTab(
    modifier: Modifier,
    exercise: Exercise,
    latestResult: Result?
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier.height(8.dp))

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

private sealed class ExerciseDetailItem {
    /**
     * Display the points without a current score result the user has.
     */
    class PointsNoSubmission(val points: Float) : ExerciseDetailItem()

    /**
     * Display the points of the exercise in combination with the user's current achieved points.
     */
    class PointsWithSubmission(val userPoints: Float, val points: Float) : ExerciseDetailItem()

    class AssessmentType(val assessmentType: Exercise.AssessmentType) : ExerciseDetailItem()

    class SubmissionDueDate(val dueDate: Instant) : ExerciseDetailItem()

    object ExerciseType : ExerciseDetailItem()
}

/**
 * Display the exercise info.
 * Includes: Max points reachable, the assessment type and submission status.
 */
@Composable
private fun ExerciseDetails(modifier: Modifier, exercise: Exercise, latestResult: Result?) {
    //Calculate what needs to be displayed.
    val detailsToDisplay = remember(exercise) {
        //Which details should be displayed
        val exerciseDetailList = mutableListOf<ExerciseDetailItem>(ExerciseDetailItem.ExerciseType)

        val points = exercise.maxPoints
        val currentUserPoints =
            exercise.studentParticipations.orEmpty()
                .firstOrNull()?.results?.maxBy { it.completionDate ?: Instant.fromEpochSeconds(0L) }
                ?.score

        if (points != null && currentUserPoints == null) {
            exerciseDetailList += ExerciseDetailItem.PointsNoSubmission(points)
        } else if (points != null && currentUserPoints != null) {
            exerciseDetailList += ExerciseDetailItem.PointsWithSubmission(currentUserPoints, points)
        }

        val assessmentType = exercise.assessmentType
        if (assessmentType != null) {
            exerciseDetailList += ExerciseDetailItem.AssessmentType(assessmentType)
        }

        val dueDate = exercise.getDueDate(exercise.studentParticipations.orEmpty().firstOrNull())
        if (dueDate != null) {
            exerciseDetailList += ExerciseDetailItem.SubmissionDueDate(dueDate)
        }

        exerciseDetailList
    }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.exercise_info_title),
                style = MaterialTheme.typography.titleSmall
            )

            val detailsComponentModifier = Modifier.fillMaxWidth()
            detailsToDisplay.forEachIndexed { index, exerciseDetailItem ->
                when (exerciseDetailItem) {
                    is ExerciseDetailItem.PointsNoSubmission -> {
                        ExerciseDetailsTextComponent(
                            modifier = detailsComponentModifier,
                            title = stringResource(id = R.string.exercise_info_points),
                            value = exerciseDetailItem.points.toString()
                        )
                    }
                    is ExerciseDetailItem.PointsWithSubmission -> {
                        ExerciseDetailsTextComponent(
                            modifier = detailsComponentModifier,
                            title = stringResource(id = R.string.exercise_info_reached_points),
                            value = stringResource(
                                id = R.string.exercise_info_reached_points_value,
                                exerciseDetailItem.userPoints,
                                exerciseDetailItem.points
                            )
                        )
                    }
                    is ExerciseDetailItem.AssessmentType -> {
                        ExerciseDetailsTextComponent(
                            modifier = detailsComponentModifier,
                            title = stringResource(id = R.string.exercise_info_assessment),
                            value = getAssessmentTypeReadableName(exerciseDetailItem.assessmentType)
                        )
                    }
                    is ExerciseDetailItem.SubmissionDueDate -> {
                        ExerciseDetailsTextComponent(
                            modifier = detailsComponentModifier,
                            title = stringResource(id = R.string.exercise_info_due_date),
                            value = getRelativeTime(to = exerciseDetailItem.dueDate).toString()
                        )
                    }
                    ExerciseDetailItem.ExerciseType -> {
                        val typeName = getExerciseTypeReadableName(exercise = exercise)
                        val (exerciseTypeString, inlineContent) = remember(exercise) {
                            val text = buildAnnotatedString {
                                appendInlineContent(id = "exerciseTypeIcon")
                                append(" ")
                                append(typeName)
                            }

                            val inlineContent = mapOf(
                                "exerciseTypeIcon" to InlineTextContent(
                                    Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
                                ) {
                                    Icon(
                                        imageVector = getExerciseTypeIcon(exercise),
                                        contentDescription = null
                                    )
                                }
                            )

                            text to inlineContent
                        }

                        ExerciseDetailsComponent(
                            modifier = detailsComponentModifier,
                            title = stringResource(id = R.string.exercise_info_exercise_type),
                        ) {
                            Text(
                                text = exerciseTypeString,
                                inlineContent = inlineContent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (index != detailsToDisplay.size - 1) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }
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
private fun getExerciseTypeReadableName(exercise: Exercise): String {
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