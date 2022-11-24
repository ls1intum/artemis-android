package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

fun NavController.navigateToExercise(exerciseId: Int, builder: NavOptionsBuilder.() -> Unit) {
    navigate("exercise/$exerciseId", builder)
}

fun NavGraphBuilder.exercise(onNavigateBack: () -> Unit) {
    composable(
        route = "exercise/{exerciseId}",
        arguments = listOf(navArgument("exerciseId") {
            type = NavType.IntType
            nullable = false
        }
        )
    ) { backStackEntry ->
        val exerciseId =
            backStackEntry.arguments?.getInt("exerciseId")
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
        }
    }
}

@Composable
internal fun <T> ExerciseDataStateUi(modifier: Modifier, value: DataState<T>, viewModel: ExerciseViewModel, onSuccess: @Composable BoxScope.(T) -> Unit) {
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