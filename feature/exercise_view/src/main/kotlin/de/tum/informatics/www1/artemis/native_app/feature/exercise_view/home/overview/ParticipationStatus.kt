package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.isPreliminary
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

/**
 * Display the default participation status ui in combination with enhancing options.
 * These additional options are, e.g, a button to inspect the result.
 * @param onClickViewResult called when the user wants to view their latest result.
 */
@Composable
internal fun ParticipationStatusUi(
    modifier: Modifier,
    exercise: Exercise,
    gradedParticipation: Participation?,
    showResult: Boolean = true,
    onClickStartExercise: () -> Unit,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    onClickViewResult: () -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit
) {
    val templateStatus: ResultTemplateStatus? =
        if (exercise.studentParticipations.isNullOrEmpty()) {
            null
        } else {
            computeTemplateStatus(
                exercise = exercise,
                participation = exercise.studentParticipations.orEmpty().first(),
                result = null,
                showUngradedResults = true,
                personal = true
            ).collectAsState(initial = ResultTemplateStatus.NoResult).value
        }

    FlowRow(modifier = modifier, mainAxisSpacing = 8.dp, crossAxisAlignment = FlowCrossAxisAlignment.Center) {
        ExerciseActionButtons(
            modifier = Modifier,
            exercise = exercise,
            gradedParticipation = gradedParticipation,
            onClickStartExercise = onClickStartExercise,
            onClickPracticeQuiz = onClickPracticeQuiz,
            onClickOpenQuiz = onClickOpenQuiz,
            onClickStartQuiz = onClickStartQuiz,
            templateStatus = templateStatus,
            onClickOpenTextExercise = onClickOpenTextExercise,
            showResult = showResult,
            onClickViewResult = onClickViewResult
        )

        ParticipationStatusUi(
            modifier = Modifier,
            exercise = exercise,
            getTemplateStatus = {
                checkNotNull(templateStatus) { "template status must not be null as participation status has a participation" }
                templateStatus
            }
        )
    }
}

/**
 * This composable composes up to two buttons. The modifier parameter is applied to every button
 * individually.
 */
@Composable
private fun ExerciseActionButtons(
    modifier: Modifier,
    exercise: Exercise,
    gradedParticipation: Participation?,
    onClickStartExercise: () -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    templateStatus: ResultTemplateStatus?,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    showResult: Boolean,
    onClickViewResult: () -> Unit
) {
    // TODO: Team mode is currently not supported. Therefore, the buttons are disabled in team mode exercises

    if (exercise is TextExercise) {
        if (gradedParticipation == null && isStartExerciseAvailable(exercise)) {
            Button(
                modifier = modifier,
                onClick = onClickStartExercise,
                enabled = !exercise.teamMode
            ) {
                Text(
                    text = stringResource(id = R.string.exercise_participation_status_view_start_exercise_button)
                )
            }
        }
    }

    if (exercise is QuizExercise) {
        if (isStartPracticeAvailable(exercise = exercise)) {
            Button(
                modifier = modifier,
                onClick = { onClickPracticeQuiz() }
            ) {
                Text(
                    text = stringResource(id = R.string.exercise_participation_status_view_practice_quiz_button)
                )
            }
        }

        val openQuizAvailable =
            exercise.notStartedC || gradedParticipation?.initializationState == Participation.InitializationState.INITIALIZED
        val startQuizAvailable = exercise.isUninitializedC

        if (openQuizAvailable || startQuizAvailable) {
            Button(
                modifier = modifier,
                onClick = {
                    if (openQuizAvailable) onClickOpenQuiz()
                    else onClickStartQuiz()
                }
            ) {
                Text(
                    text = stringResource(
                        id = if (openQuizAvailable) R.string.exercise_participation_status_view_open_quiz_button
                        else R.string.exercise_participation_status_view_start_quiz_button
                    )
                )
            }
        }
    }

    if (templateStatus != null) {
        if (exercise is TextExercise) {
            if (gradedParticipation?.initializationState == Participation.InitializationState.INITIALIZED) {
                Button(
                    modifier = modifier,
                    onClick = {
                        onClickOpenTextExercise(
                            gradedParticipation.id ?: return@Button
                        )
                    },
                    enabled = !exercise.teamMode
                ) {
                    Text(
                        text = stringResource(id = R.string.exercise_participation_status_view_open_exercise_button)
                    )
                }
            }

            if (gradedParticipation?.initializationState == Participation.InitializationState.FINISHED &&
                (gradedParticipation.results.isNullOrEmpty() || !showResult)
            ) {
                Button(
                    modifier = modifier,
                    onClick = {
                        onClickOpenTextExercise(
                            gradedParticipation.id ?: return@Button
                        )
                    },
                    enabled = !exercise.teamMode
                ) {
                    Text(
                        text = stringResource(id = R.string.exercise_participation_status_view_view_submission_button)
                    )
                }
            }
        }

        if (templateStatus is ResultTemplateStatus.WithResult && canShowResultDetails(
                null,
                templateStatus.result
            )
        ) {
            Button(
                modifier = modifier,
                onClick = onClickViewResult
            ) {
                Text(text = stringResource(id = R.string.exercise_participation_status_view_result_button))
            }
        }
    }
}

@Composable
private fun canShowResultDetails(
    @Suppress("SameParameterValue") submission: Submission?,
    result: Result
): Boolean {
    if (result.isPreliminary.collectAsState(initial = false).value) return true

    if (result.submission != null && submission is ProgrammingSubmission && submission.buildFailed == true) return true
    return result.hasFeedback == true
}

@Composable
private fun isStartExerciseAvailable(exercise: Exercise): Boolean {
    return exercise.isStartExerciseAvailable.collectAsState(initial = false).value
}

/**
 * The start practice button should be available for programming and quiz exercises
 * - For quizzes when they are open for practice and the regular work periode is over
 * - For programming exercises when it's after the due date
 */
@Composable
private fun isStartPracticeAvailable(exercise: Exercise): Boolean {
    return when (exercise) {
        is QuizExercise -> {
            exercise.isOpenForPractice == true && hasQuizEnded(exercise)
        }

        is ProgrammingExercise -> {
            val dueDate = exercise.dueDate
            dueDate != null && dueDate.hasPassed() && !exercise.teamMode
        }

        else -> false
    }
}

@Composable
private fun hasQuizEnded(quizExercise: QuizExercise): Boolean =
    quizExercise.hasEnded.collectAsState(initial = false).value