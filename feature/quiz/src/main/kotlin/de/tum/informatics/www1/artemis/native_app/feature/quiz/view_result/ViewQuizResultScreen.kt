package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
private data class ViewQuizResultScreen(val courseId: Long, val exerciseId: Long)

fun NavController.navigateToQuizResult(
    courseId: Long,
    exerciseId: Long
) {
    navigate(ViewQuizResultScreen(courseId, exerciseId))
}

fun NavGraphBuilder.quizResults(onRequestLeaveQuizResults: () -> Unit) {
    animatedComposable<ViewQuizResultScreen> { backStackEntry ->
        val route: ViewQuizResultScreen = backStackEntry.toRoute()

        ViewQuizResultScreen(
            modifier = Modifier.fillMaxSize(),
            exerciseId = route.exerciseId,
            quizType = QuizType.ViewResults,
            onNavigateUp = onRequestLeaveQuizResults
        )
    }
}

@Composable
internal fun ViewQuizResultScreen(
    modifier: Modifier,
    exerciseId: Long,
    quizType: QuizType.ViewableQuizType,
    onNavigateUp: () -> Unit
) {
    val viewModel: QuizResultViewModel = koinViewModel { parametersOf(exerciseId, quizType) }

    val quizExercise by viewModel.quizExerciseDataState.collectAsState()
    val submission by viewModel.submission.collectAsState()
    val result by viewModel.result.collectAsState()
    val quizQuestions by viewModel.quizQuestionsWithData.collectAsState()
    val maxPoints by viewModel.maxPoints.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = { Text(text = stringResource(id = R.string.quiz_results_title)) },
                navigationIcon = {
                    NavigationBackButton(onNavigateUp)
                },
                actions = {
                    IconButton(onClick = viewModel::retryLoadExercise) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = joinMultiple(
                quizExercise,
                submission,
                result,
                quizQuestions,
                maxPoints,
                ::ResultData
            ),
            loadingText = stringResource(id = R.string.quiz_result_loading),
            failureText = stringResource(id = R.string.quiz_result_failure),
            retryButtonText = stringResource(id = R.string.quiz_result_try_again),
            onClickRetry = viewModel::retryLoadExercise
        ) { data ->
            QuizResultUi(
                modifier = Modifier.fillMaxSize(),
                quizQuestions = data.quizExercise.quizQuestions,
                quizQuestionsWithData = data.quizQuestions,
                achievedPoints = data.submission.scoreInPoints ?: 0.0,
                maxPoints = data.maxPoints,
                quizTitle = data.quizExercise.title.orEmpty()
            )
        }
    }
}

private fun <A, B, C, D, E, F> joinMultiple(
    first: DataState<A>,
    second: DataState<B>,
    third: DataState<C>,
    fourth: DataState<D>,
    fifth: DataState<E>,
    transform: (A, B, C, D, E) -> F
): DataState<F> {
    return (((first join second) join (third join fourth)) join fifth).bind { (x, f5) ->
        val (a, b) = x
        val (f, s) = a
        val (t, f4) = b
        transform(f, s, t, f4, f5)
    }
}

private data class ResultData(
    val quizExercise: QuizExercise,
    val submission: QuizSubmission,
    val result: Result,
    val quizQuestions: List<QuizQuestionData<*>>,
    val maxPoints: Int
)