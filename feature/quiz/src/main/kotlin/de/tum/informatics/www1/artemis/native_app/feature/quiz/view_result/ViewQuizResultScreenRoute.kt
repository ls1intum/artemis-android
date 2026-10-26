package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import org.koin.compose.koinInject
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ProvideReviewImages
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
private data class ViewQuizResultScreenRoute(val courseId: Long, val exerciseId: Long)

fun NavController.navigateToQuizResult(
    courseId: Long,
    exerciseId: Long
) {
    navigate(ViewQuizResultScreenRoute(courseId, exerciseId))
}

fun NavGraphBuilder.quizResults() {
    animatedComposable<ViewQuizResultScreenRoute> { backStackEntry ->
        val route: ViewQuizResultScreenRoute = backStackEntry.toRoute()

        ViewQuizResultScreen(
            modifier = Modifier.fillMaxSize(),
            exerciseId = route.exerciseId,
            quizType = QuizType.ViewResults
        )
    }
}

@Composable
internal fun ViewQuizResultScreen(
    modifier: Modifier,
    exerciseId: Long,
    quizType: QuizType.ViewableQuizType
) {
    val contextProvider: ArtemisContextProvider = koinInject()
    val context by contextProvider.collectArtemisContextAsState()
    val session = remember { context as? ArtemisContext.LoggedIn }
    var denied by remember(session) { mutableStateOf(false) }
    if (session == null || context !is ArtemisContext.LoggedIn || session.serverUrl != context.serverUrl || session.authToken != (context as? ArtemisContext.LoggedIn)?.authToken || session.loginName != (context as? ArtemisContext.LoggedIn)?.loginName || denied) {
        Text(stringResource(R.string.quiz_review_access_unavailable), Modifier.padding(24.dp))
        return
    }
    val viewModel: QuizResultViewModel = koinViewModel { parametersOf(exerciseId, quizType) }

    val quizExercise by viewModel.quizExerciseDataState.collectAsState()
    val submission by viewModel.submission.collectAsState()
    val result by viewModel.result.collectAsState()
    val quizQuestions by viewModel.quizQuestionsWithData.collectAsState()
    val maxPoints by viewModel.maxPoints.collectAsState()
    val attempts by viewModel.reviewAttempts.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = { Text(text = stringResource(id = R.string.quiz_results_title)) },
                navigationIcon = {
                    NavigationBackButton()
                },
                actions = {
                    IconButton(onClick = viewModel::onRequestReload) {
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
            onClickRetry = viewModel::onRequestReload
        ) { data ->
            ProvideReviewImages(session, onDenied = { denied = true }) {
                Column(Modifier.fillMaxSize()) {
                    QuizReviewAttemptControls(data, quizType, attempts, viewModel::onSelectReviewAttempt)
                    key(data.submission.id) {
                        QuizResultUi(
                            modifier = Modifier.weight(1f),
                            quizQuestions = data.quizExercise.quizQuestions,
                            quizQuestionsWithData = data.quizQuestions,
                            achievedPoints = data.submission.scoreInPoints,
                            maxPoints = quizReviewMaximum(data.quizExercise),
                            quizTitle = data.quizExercise.title.orEmpty(),
                            precision = data.quizExercise.course?.accuracyOfScores?.toInt() ?: 1,
                        )
                    }
                }
            }
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
    val maxPoints: Double
)
@Composable
private fun QuizReviewAttemptControls(data: ResultData, quizType: QuizType.ViewableQuizType, attempts: List<QuizReviewAttempt>, onSelect: (QuizReviewAttempt) -> Unit) {
    val selected = attempts.firstOrNull { it.submissionId == data.submission.id }
    val practice = quizType is QuizType.PracticeResults || selected?.practice == true
    val state = stringResource(if (practice) R.string.quiz_review_practice else if (data.result.rated == true) R.string.quiz_review_graded else R.string.quiz_review_unrated)
    val submitted = data.submission.submissionDate?.let { java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(it.toEpochMilliseconds())) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(listOfNotNull(state, submitted).joinToString(" · "), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        if (attempts.size > 1) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.quiz_review_attempts)) }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    attempts.forEach { attempt ->
                        DropdownMenuItem(text = {
                            val label = stringResource(if (attempt.practice) R.string.quiz_review_practice else if (attempt.rated == true) R.string.quiz_review_graded else R.string.quiz_review_unrated)
                            val date = attempt.submissionDate?.let { java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(it.toEpochMilliseconds())) }
                            Text(listOfNotNull(label, date ?: "#${attempt.submissionId}").joinToString(" · "))
                        }, onClick = { expanded = false; onSelect(attempt) })
                    }
                }
            }
        }
    }
}
