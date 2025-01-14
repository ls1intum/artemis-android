package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.hasEnded
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.isStartExerciseAvailable
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
    templateStatus: ResultTemplateStatus? = LocalTemplateStatusProvider.current(),
    showResult: Boolean,
    actions: ExerciseActions
) {
    // TODO: Team mode is currently not supported. Therefore, the buttons are disabled in team mode exercises

    val latestParticipation = exercise.getSpecificStudentParticipation(false)
    val isStartExerciseAvailable = exercise.isStartExerciseAvailable.collectAsState(initial = false).value

    if (exercise is TextExercise) {
        if (latestParticipation == null && isStartExerciseAvailable) {
            Button(
                modifier = modifier,
                onClick = actions.onClickStartTextExercise,
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
                onClick = actions.onClickPracticeQuiz
            ) {
                Text(
                    text = stringResource(id = R.string.exercise_actions_practice_quiz_button)
                )
            }
        }

        val openQuizAvailable =
            exercise.notStartedC || latestParticipation?.initializationState == Participation.InitializationState.INITIALIZED
        val startQuizAvailable = exercise.isUninitializedC

        if (openQuizAvailable || startQuizAvailable) {
            // TODO: Quiz participation temporarily disabled. See https://github.com/ls1intum/artemis-android/issues/107
//            Button(
//                modifier = modifier,
//                onClick = {
//                    if (openQuizAvailable) actions.onClickOpenQuiz()
//                    else actions.onClickStartQuiz()
//                }
//            ) {
//                Text(
//                    text = stringResource(
//                        id = if (openQuizAvailable) R.string.exercise_actions_open_quiz_button
//                        else R.string.exercise_actions_start_quiz_button
//                    )
//                )
//            }
        }
    }

    if (templateStatus != null) {
        if (exercise is TextExercise) {
            if (latestParticipation?.initializationState == Participation.InitializationState.INITIALIZED) {
                Button(
                    modifier = modifier,
                    onClick = {
                        actions.onClickOpenTextExercise(
                            latestParticipation.id ?: return@Button
                        )
                    },
                    enabled = !exercise.teamMode
                ) {
                    Text(
                        text = stringResource(id = R.string.exercise_actions_open_exercise_button)
                    )
                }
            }

            if (latestParticipation?.initializationState == Participation.InitializationState.FINISHED &&
                (latestParticipation.results.isNullOrEmpty() || !showResult)
            ) {
                Button(
                    modifier = modifier,
                    onClick = {
                        actions.onClickOpenTextExercise(
                            latestParticipation.id ?: return@Button
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
                onClick = if (exercise is QuizExercise) actions.onClickViewQuizResults else actions.onClickViewResult
            ) {
                Text(text = stringResource(id = R.string.exercise_actions_view_result_button))
            }
        }
    }
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

data class ExerciseActions(
    val onClickStartTextExercise: () -> Unit,
    val onClickPracticeQuiz: () -> Unit,
    val onClickOpenQuiz: () -> Unit,
    val onClickStartQuiz: () -> Unit,
    val onClickOpenTextExercise: (participationId: Long) -> Unit,
    val onClickViewResult: () -> Unit,
    val onClickViewQuizResults: () -> Unit,
)

class BoundExerciseActions(
    val onClickStartTextExercise: (exerciseId: Long) -> Unit,
    val onClickPracticeQuiz: (exerciseId: Long) -> Unit,
    val onClickOpenQuiz: (exerciseId: Long) -> Unit,
    val onClickStartQuiz: (exerciseId: Long) -> Unit,
    val onClickOpenTextExercise: (exerciseId: Long, participationId: Long) -> Unit,
    val onClickViewResult: (exerciseId: Long) -> Unit,
    val onClickViewQuizResults: (exerciseId: Long) -> Unit,
) {
    fun getUnbound(exerciseId: Long): ExerciseActions = ExerciseActions(
        onClickStartTextExercise = { onClickStartTextExercise(exerciseId) },
        onClickPracticeQuiz = { onClickPracticeQuiz(exerciseId) },
        onClickOpenQuiz = { onClickOpenQuiz(exerciseId) },
        onClickStartQuiz = { onClickStartQuiz(exerciseId) },
        onClickOpenTextExercise = { onClickOpenTextExercise(exerciseId, it) },
        onClickViewResult = { onClickViewResult(exerciseId) },
        onClickViewQuizResults = { onClickViewQuizResults(exerciseId) }
    )
}