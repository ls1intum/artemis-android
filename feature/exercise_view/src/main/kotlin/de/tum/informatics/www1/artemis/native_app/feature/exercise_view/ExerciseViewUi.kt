package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.ExerciseScreen
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise.TextExerciseParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result.ViewResultScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Display the exercise view
 */
private const val NESTED_HOME_DESTINATION = "home"

/**
 * View the latest result
 */
private const val NESTED_EXERCISE_RESULT_DESTINATION = "view_result"

private const val NESTED_PARTICIPATE_TEXT_EXERCISE_DESTINATION =
    "participate/text_exercise/{participationId}"

fun NavController.navigateToExercise(exerciseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate("exercise?$exerciseId", builder)
}

fun NavGraphBuilder.exercise(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit
) {
    composable(
        route = "exercise?{exerciseId}",
        arguments = listOf(navArgument("exerciseId") {
            type = NavType.LongType
            nullable = false
        }
        )
    ) { backStackEntry ->
        val exerciseId =
            backStackEntry.arguments?.getLong("exerciseId")
        checkNotNull(exerciseId)

        val exerciseViewModel = koinViewModel<ExerciseViewModel> { parametersOf(exerciseId) }

        val nestedNavController = rememberNavController()

        NavHost(navController = nestedNavController, startDestination = NESTED_HOME_DESTINATION) {
            composable(NESTED_HOME_DESTINATION) {
                ExerciseScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onNavigateBack = onNavigateBack,
                    onViewResult = {
                        nestedNavController.navigate(NESTED_EXERCISE_RESULT_DESTINATION)
                    },
                    navController = navController,
                    onViewTextExerciseParticipationScreen = { participationId ->
                        nestedNavController.navigate("participate/text_exercise/$participationId")
                    },
                    onParticipateInQuiz = { courseId, isPractice ->
                        onParticipateInQuiz(courseId, exerciseId, isPractice)
                    }
                )
            }

            composable(NESTED_EXERCISE_RESULT_DESTINATION) {
                ViewResultScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onCloseResult = nestedNavController::navigateUp
                )
            }

            composable(
                NESTED_PARTICIPATE_TEXT_EXERCISE_DESTINATION,
                arguments = listOf(
                    navArgument(
                        "participationId"
                    ) {
                        type = NavType.LongType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val participationId: Long = backStackEntry.arguments?.getLong("participationId")
                    ?: throw IllegalArgumentException()

                TextExerciseParticipationScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    participationId = participationId,
                    onNavigateUp = nestedNavController::navigateUp
                )
            }
        }
    }
}

@Composable
internal fun <T> ExerciseDataStateUi(
    modifier: Modifier,
    value: DataState<T>,
    viewModel: ExerciseViewModel,
    onSuccess: @Composable BoxScope.(T) -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = value,
        loadingText = stringResource(id = R.string.exercise_view_loading),
        failureText = stringResource(id = R.string.exercise_view_failure),
        suspendedText = stringResource(id = R.string.exercise_view_suspended),
        retryButtonText = stringResource(id = R.string.exercise_view_try_again),
        onClickRetry = { viewModel.requestReloadExercise() },
        successUi = onSuccess
    )
}