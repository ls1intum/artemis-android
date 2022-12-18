package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.WaitForQuizStartScreen

@Composable
internal fun QuizParticipationUi(modifier: Modifier, viewModel: QuizParticipationViewModel) {
    val exerciseDataState = viewModel.quizExerciseDataState.collectAsState().value
    val isWaitingForQuizStart = viewModel.waitingForQuizStart.collectAsState(initial = false).value
    val isConnected = viewModel.isConnected.collectAsState(initial = false).value
    val batch = viewModel.quizBatch.collectAsState(initial = null).value

    BasicDataStateUi(
        modifier = modifier,
        dataState = exerciseDataState,
        loadingText = stringResource(id = R.string.quiz_participation_loading),
        failureText = stringResource(id = R.string.quiz_participation_failure),
        suspendedText = stringResource(id = R.string.quiz_participation_suspended),
        retryButtonText = stringResource(id = R.string.quiz_participation_retry),
        onClickRetry = viewModel::retryLoadExercise
    ) { quizExercise ->
        if (isWaitingForQuizStart) {
            WaitForQuizStartScreen(
                modifier = Modifier.fillMaxSize(),
                exercise = quizExercise,
                isConnected = isConnected,
                batch = batch,
                onRequestRefresh = {
                    viewModel.retryLoadExercise()
                    viewModel.reconnectWebsocket()
                }
            )
        }
    }
}