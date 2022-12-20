package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.WaitForQuizStartScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.WorkOnQuizQuestionsScreen

@Composable
internal fun QuizParticipationUi(modifier: Modifier, viewModel: QuizParticipationViewModel) {
    val exerciseDataState = viewModel.quizExerciseDataState.collectAsState().value
    val isWaitingForQuizStart = viewModel.waitingForQuizStart.collectAsState(initial = false).value
    val isConnected = viewModel.isConnected.collectAsState(initial = false).value
    val batch = viewModel.quizBatch.collectAsState(initial = null).value

    var joinBatchError: JoinBatchErrorType? by rememberSaveable { mutableStateOf(null) }

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
                },
                onClickStartQuiz = {
                    viewModel.startBatch {
                        joinBatchError = JoinBatchErrorType.START
                    }
                },
                onClickJoinBatch = { passcode ->
                    viewModel.joinBatch(passcode) {
                        joinBatchError = JoinBatchErrorType.JOIN
                    }
                }
            )
        } else {
            val questionWithData by viewModel.quizQuestionsWithData.collectAsState(initial = emptyList())
            val lastSubmission by viewModel.latestSubmission.collectAsState()
            val endDate by viewModel.endDate.collectAsState(initial = null)

            WorkOnQuizQuestionsScreen(
                modifier = Modifier.fillMaxSize(),
                questionsWithData = questionWithData,
                lastSubmissionTime = lastSubmission.submissionDate,
                endDate = endDate
            )
        }
    }

    val currentJoinBatchError = joinBatchError
    if (currentJoinBatchError != null) {
        AlertDialog(
            onDismissRequest = { joinBatchError = null },
            text = {
                Text(
                    text = stringResource(
                        id = when (currentJoinBatchError) {
                            JoinBatchErrorType.JOIN -> R.string.quiz_participation_wait_for_start_join_batch_error_message
                            JoinBatchErrorType.START -> R.string.quiz_participation_wait_for_start_start_batch_error_message
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { joinBatchError = null }) {
                    Text(text = stringResource(id = R.string.quiz_participation_wait_for_start_batch_error_positive_button))
                }
            }
        )
    }
}

private enum class JoinBatchErrorType {
    JOIN,
    START
}