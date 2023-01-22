package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed

/**
 * This composable composes up to two buttons. The modifier parameter is applied to every button
 * individually.
 */
@Composable
fun ExerciseActionButtons(
    modifier: Modifier,
    exercise: Exercise,
    gradedParticipation: Participation?,
    onClickStartExercise: () -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    templateStatus: ResultTemplateStatus? = LocalTemplateStatusProvider.current(),
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
                    text = stringResource(id = R.string.exercise_actions_start_exercise_button)
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
                    text = stringResource(id = R.string.exercise_actions_practice_quiz_button)
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
                        id = if (openQuizAvailable) R.string.exercise_actions_open_quiz_button
                        else R.string.exercise_actions_start_quiz_button
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
                        text = stringResource(id = R.string.exercise_actions_open_exercise_button)
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
                        text = stringResource(id = R.string.exercise_actions_view_submission_button)
                    )
                }
            }
        }

        if (templateStatus is ResultTemplateStatus.WithResult) {
            Button(
                modifier = modifier,
                onClick = onClickViewResult
            ) {
                Text(text = stringResource(id = R.string.exercise_actions_view_result_button))
            }
        }
    }
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