package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.generateLinks
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.ExerciseScreen
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise.TextExerciseParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result.ViewResultScreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder
import java.net.URLEncoder

object ExerciseViewDestination {
    const val EXERCISE_VIEW_ROUTE = "exercise/{exerciseId}/{viewMode}"

    /**
     * Set this to true to on the backStackEntry and the exercise will be reloaded
     */
    const val REQUIRE_RELOAD_KEY = "requireReload"
}

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

fun NavController.navigateToExercise(
    exerciseId: Long,
    viewMode: ExerciseViewMode,
    builder: NavOptionsBuilder.() -> Unit
) {
    val viewModeAsString =
        URLEncoder.encode(Json.encodeToString(ExerciseViewMode.serializer(), viewMode), "UTF-8")

    navigate("exercise/$exerciseId/$viewModeAsString", builder)
}

fun NavGraphBuilder.exercise(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit
) {
    composable(
        route = ExerciseViewDestination.EXERCISE_VIEW_ROUTE,
        arguments = listOf(
            navArgument("exerciseId") {
                type = NavType.LongType
                nullable = false
            },
            navArgument("viewMode") {
                type = NavType.StringType
                defaultValue =
                    Json.encodeToString(ExerciseViewMode.serializer(), ExerciseViewMode.Overview)
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "artemis://exercises/{exerciseId}"
            }
        ) + generateLinks("courses/{courseId}/exercises/{exerciseId}")
    ) { backStackEntry ->
        val exerciseId =
            backStackEntry.arguments?.getLong("exerciseId")
        checkNotNull(exerciseId)

        val viewMode: ExerciseViewMode = backStackEntry.arguments?.getString("viewMode")?.let {
            Json.decodeFromString(URLDecoder.decode(it, "UTF-8"))
        } ?: ExerciseViewMode.Overview

        val exerciseViewModel = koinViewModel<ExerciseViewModel> { parametersOf(exerciseId) }

        val nestedNavController = rememberNavController()

        SideEffect {
            val isReloadRequested =
                backStackEntry.savedStateHandle.remove<Boolean>(ExerciseViewDestination.REQUIRE_RELOAD_KEY)

            if (isReloadRequested == true) {
                exerciseViewModel.requestReloadExercise()
            }
        }

        val startDestination = when (viewMode) {
            ExerciseViewMode.Overview -> NESTED_HOME_DESTINATION
            is ExerciseViewMode.TextParticipation -> NESTED_PARTICIPATE_TEXT_EXERCISE_DESTINATION
            ExerciseViewMode.ViewResult -> NESTED_EXERCISE_RESULT_DESTINATION
        }

        val nestedNavigateUp: () -> Unit = {
            if (nestedNavController.previousBackStackEntry != null) {
                nestedNavController.navigateUp()
            } else {
                onNavigateBack()
            }
        }

        NavHost(navController = nestedNavController, startDestination = startDestination) {
            composable(NESTED_HOME_DESTINATION) {
                ExerciseScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onNavigateBack = nestedNavigateUp,
                    onViewResult = {
                        nestedNavController.navigate(NESTED_EXERCISE_RESULT_DESTINATION)
                    },
                    navController = navController,
                    onViewTextExerciseParticipationScreen = { participationId ->
                        nestedNavController.navigate(createTextParticipationRoute(participationId))
                    },
                    onParticipateInQuiz = { courseId, isPractice ->
                        onParticipateInQuiz(courseId, exerciseId, isPractice)
                    },
                    onClickViewQuizResults = { courseId ->
                        onClickViewQuizResults(courseId, exerciseId)
                    }
                )
            }

            composable(NESTED_EXERCISE_RESULT_DESTINATION) {
                ViewResultScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onCloseResult = nestedNavigateUp
                )
            }

            composable(
                NESTED_PARTICIPATE_TEXT_EXERCISE_DESTINATION,
                arguments = listOf(
                    navArgument(
                        "participationId"
                    ) {
                        type = NavType.LongType
                        if (viewMode is ExerciseViewMode.TextParticipation) {
                            defaultValue = viewMode.participationId
                        } else nullable = false
                    }
                )
            ) { backStackEntry ->
                val participationId: Long = backStackEntry.arguments?.getLong("participationId")
                    ?: throw IllegalArgumentException()

                val exerciseDataState by exerciseViewModel.exerciseDataState.collectAsState()

                EmptyDataStateUi(dataState = exerciseDataState) { exercise ->
                    TextExerciseParticipationScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = koinViewModel { parametersOf(exercise.id, participationId) },
                        exercise = exercise,
                        onNavigateUp = nestedNavigateUp
                    )
                }
            }
        }
    }
}

private fun createTextParticipationRoute(participationId: Long) =
    "participate/text_exercise/$participationId"

@Composable
internal fun <T> ExerciseDataStateUi(
    modifier: Modifier,
    value: DataState<T>,
    onSuccess: @Composable (BoxScope.(T) -> Unit),
    onClickRetry: () -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = value,
        loadingText = stringResource(id = R.string.exercise_view_loading),
        failureText = stringResource(id = R.string.exercise_view_failure),
        retryButtonText = stringResource(id = R.string.exercise_view_try_again),
        onClickRetry = onClickRetry,
        successUi = onSuccess
    )
}

@Serializable
sealed interface ExerciseViewMode {
    @Serializable
    @SerialName("overview")
    object Overview : ExerciseViewMode

    @Serializable
    @SerialName("text_participation")
    data class TextParticipation(val participationId: Long) : ExerciseViewMode

    @Serializable
    @SerialName("view_result")
    object ViewResult : ExerciseViewMode
}

internal val DataState<Exercise>.courseId: Long?
    @Composable get() = remember(this) {
        derivedStateOf {
            bind { it.course?.id }.orNull()
        }
    }.value