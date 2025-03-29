package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.KSerializableNavType
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.ExerciseScreen
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult.ViewResultScreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

object ExerciseViewDestination {
    /**
     * Set this to true to on the backStackEntry and the exercise will be reloaded
     */
    const val REQUIRE_RELOAD_KEY = "requireReload"
}

@Serializable
sealed interface ExerciseViewUiNestedNavigation {

    /**
     * Display the exercise view
     */
    @Serializable
    data object Home : ExerciseViewUiNestedNavigation

    /**
     * View the latest result
     */
    @Serializable
    data object Result : ExerciseViewUiNestedNavigation

    @Serializable
    data class ParticipateTextExercise(val participationId: Long) : ExerciseViewUiNestedNavigation
}

@Serializable
data class ExerciseViewUi(
    val exerciseId: Long,
    val viewMode: ExerciseViewMode = ExerciseViewMode.Overview,
)

fun NavController.navigateToExercise(
    exerciseId: Long,
    viewMode: ExerciseViewMode,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(ExerciseViewUi(exerciseId, viewMode), builder)
}

fun NavGraphBuilder.exercise(
    onNavigateBack: () -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit
) {
    animatedComposable<ExerciseViewUi>(
        typeMap = mapOf(
            typeOf<ExerciseViewMode>() to KSerializableNavType(
                isNullableAllowed = false,
                ExerciseViewMode.Overview.serializer()
            )
        ),
        deepLinks = ExerciseDeeplinks.ToExercise.generateLinks() +
                ExerciseDeeplinks.ToExerciseCourseAgnostic.generateLinks(),
    ) { backStackEntry ->
        val route: ExerciseViewUi = backStackEntry.toRoute()

        val exerciseId = route.exerciseId
        val viewMode: ExerciseViewMode = route.viewMode

        val exerciseViewModel = koinViewModel<ExerciseViewModel> { parametersOf(exerciseId) }

        val nestedNavController = rememberNavController()

        SideEffect {
            val isReloadRequested =
                backStackEntry.savedStateHandle.remove<Boolean>(ExerciseViewDestination.REQUIRE_RELOAD_KEY)

            if (isReloadRequested == true) {
                exerciseViewModel.requestReloadExercise()
            }
        }

        val startDestination: ExerciseViewUiNestedNavigation = when (viewMode) {
            ExerciseViewMode.Overview -> ExerciseViewUiNestedNavigation.Home
            is ExerciseViewMode.TextParticipation -> ExerciseViewUiNestedNavigation.ParticipateTextExercise(
                participationId = viewMode.participationId
            )

            ExerciseViewMode.ViewResult -> ExerciseViewUiNestedNavigation.Result
        }

        val nestedNavigateUp: () -> Unit = {
            if (nestedNavController.previousBackStackEntry != null) {
                nestedNavController.navigateUp()
            } else {
                onNavigateBack()
            }
        }

        NavHost(navController = nestedNavController, startDestination = startDestination) {
            animatedComposable<ExerciseViewUiNestedNavigation.Home> {
                ExerciseScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onViewResult = {
                        nestedNavController.navigate(ExerciseViewUiNestedNavigation.Result)
                    },
                    onViewTextExerciseParticipationScreen = { participationId ->
                        nestedNavController.navigate(
                            ExerciseViewUiNestedNavigation.ParticipateTextExercise(
                                participationId
                            )
                        )
                    },
                    onParticipateInQuiz = { courseId, isPractice ->
                        onParticipateInQuiz(courseId, exerciseId, isPractice)
                    },
                    onClickViewQuizResults = { courseId ->
                        onClickViewQuizResults(courseId, exerciseId)
                    }
                )
            }

            animatedComposable<ExerciseViewUiNestedNavigation.Result> {
                ViewResultScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = exerciseViewModel,
                    onCloseResult = nestedNavigateUp
                )
            }

            animatedComposable<ExerciseViewUiNestedNavigation.ParticipateTextExercise> { backStackEntry ->
                val nestedRoute: ExerciseViewUiNestedNavigation.ParticipateTextExercise =
                    backStackEntry.toRoute()

                val participationId: Long = nestedRoute.participationId

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
    data object Overview : ExerciseViewMode

    @Serializable
    @SerialName("text_participation")
    data class TextParticipation(val participationId: Long) : ExerciseViewMode

    @Serializable
    @SerialName("view_result")
    data object ViewResult : ExerciseViewMode
}

internal val DataState<Exercise>.courseId: Long?
    @Composable get() = remember(this) {
        derivedStateOf {
            bind { it.course?.id }.orNull()
        }
    }.value