package de.tum.informatics.www1.artemis.native_app.feature.quiz.participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizEndedScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizEndedScreenType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.WaitForQuizStartScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.WorkOnQuizQuestionsScreen
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun QuizParticipationUi(
    modifier: Modifier,
    viewModel: QuizParticipationViewModel,
    onNavigateToInspectResult: (QuizType.ViewableQuizType) -> Unit,
    onNavigateUp: () -> Unit
) {
    val authToken by viewModel.authToken.collectAsState()
    val serverUrl: String = viewModel.serverUrl.collectAsState().value.dropLast(1)

    val exerciseDataState by viewModel.quizExerciseDataState.collectAsState()
    val isWaitingForQuizStart by viewModel.waitingForQuizStart.collectAsState(initial = false)
    val hasQuizEnded by viewModel.quizEndedStatus.collectAsState(initial = false)
    val isConnected by viewModel.isConnected.collectAsState(initial = false)
    val batch by viewModel.quizBatch.collectAsState(initial = null)

    val serverClock by viewModel.serverClock.collectAsState(initial = Clock.System)

    val result: Result? by viewModel.result.collectAsState()

    var joinBatchError: JoinBatchErrorType? by rememberSaveable { mutableStateOf(null) }

    var startBatchDeferred: Deferred<Boolean>? by remember { mutableStateOf(null) }
    var joinBatchDeferred: Deferred<Boolean>? by remember { mutableStateOf(null) }

    LaunchedEffect(result, exerciseDataState) {
        val currentResult = result
        val currentQuestionData = exerciseDataState
        if (currentResult?.id != null) {
            when (viewModel.quizType) {
                QuizType.Live -> onNavigateToInspectResult(QuizType.ViewResults)
                QuizType.Practice -> if (currentQuestionData is DataState.Success) {
                    onNavigateToInspectResult(
                        QuizType.PracticeResults(
                            currentQuestionData.data,
                            currentResult
                        )
                    )
                }
            }
        }
    }

    AwaitDeferredCompletion(job = startBatchDeferred) { successful ->
        if (!successful) {
            joinBatchError = JoinBatchErrorType.START
        }
        startBatchDeferred = null
    }

    AwaitDeferredCompletion(job = joinBatchDeferred) { successful ->
        if (!successful) {
            joinBatchError = JoinBatchErrorType.JOIN
        }
        joinBatchDeferred = null
    }

    BasicDataStateUi(
        modifier = modifier,
        dataState = exerciseDataState,
        loadingText = stringResource(id = R.string.quiz_participation_loading),
        failureText = stringResource(id = R.string.quiz_participation_failure),
        retryButtonText = stringResource(id = R.string.quiz_participation_retry),
        onClickRetry = viewModel::retryLoadExercise
    ) { quizExercise ->
        if (isWaitingForQuizStart) {
            WaitForQuizStartScreen(
                modifier = Modifier.fillMaxSize(),
                exercise = quizExercise,
                isConnected = isConnected,
                batch = batch,
                clock = serverClock,
                isStartingOrJoiningQuiz = startBatchDeferred != null || joinBatchDeferred != null,
                onRequestRefresh = {
                    viewModel.retryLoadExercise()
                    viewModel.reconnectWebsocket()
                },
                onClickStartQuiz = {
                    startBatchDeferred = viewModel.startBatch()
                },
                onClickJoinBatch = { passcode ->
                    joinBatchDeferred = viewModel.joinBatch(passcode)
                }
            )
        } else if (hasQuizEnded) {
            var submittingJob: Job? by remember { mutableStateOf(null) }

            QuizEndedScreen(
                modifier = Modifier.fillMaxSize(),
                type = when (viewModel.quizType) {
                    QuizType.Live -> QuizEndedScreenType.Live(
                        onRequestLeave = onNavigateUp
                    )

                    QuizType.Practice -> QuizEndedScreenType.Practice(
                        onRequestSubmit = {
                            submittingJob = viewModel.submit {
                                submittingJob = null
                            }
                        },
                        isSubmitting = submittingJob != null
                    )
                }
            )
        } else {
            val questionWithDataDataState by viewModel.quizQuestionsWithData.collectAsState()
            val lastSubmission by viewModel.latestSubmission.collectAsState()
            val endDate by viewModel.endDate.collectAsState(initial = null)
            val overallPoints by viewModel.overallPoints.collectAsState(initial = 0)
            val latestWebsocketSubmission by viewModel
                .latestWebsocketSubmission.collectAsState(initial = null)

            EmptyDataStateUi(
                dataState = questionWithDataDataState
            ) { questionsWithData ->

                WorkOnQuizQuestionsScreen(
                    modifier = Modifier.fillMaxSize(),
                    quizType = viewModel.quizType,
                    questionsWithData = questionsWithData,
                    lastSubmissionTime = lastSubmission.submissionDate,
                    endDate = endDate,
                    authToken = authToken,
                    serverUrl = serverUrl,
                    isConnected = isConnected,
                    overallPoints = overallPoints,
                    latestWebsocketSubmission = latestWebsocketSubmission,
                    clock = serverClock,
                    onRequestRetrySave = viewModel::requestSaveSubmissionThroughWebsocket
                )
            }
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

    if (joinBatchDeferred != null) {
        BatchJobDialog(
            message = stringResource(id = R.string.quiz_participation_wait_for_start_join_batch_dialog_message),
            confirmText = stringResource(id = R.string.quiz_participation_wait_for_start_join_batch_dialog_negative),
            onDismiss = {
                joinBatchDeferred?.cancel()
                joinBatchDeferred = null
            }
        )
    }

    if (startBatchDeferred != null) {
        BatchJobDialog(
            message = stringResource(id = R.string.quiz_participation_wait_for_start_start_batch_dialog_message),
            confirmText = stringResource(id = R.string.quiz_participation_wait_for_start_start_batch_dialog_negative),
            onDismiss = {
                startBatchDeferred?.cancel()
                startBatchDeferred = null
            }
        )
    }
}

@Composable
private fun BatchJobDialog(message: String, confirmText: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = confirmText)
            }
        }
    )
}

/**
 * Formats a time that is in the future in a relative style. Automatically updates when necessary.
 */
@Composable
fun getFormattedRelativeToFutureTimeQuizStyle(timeInFuture: Instant, clock: Clock): String {
    val scope = rememberCoroutineScope()

    val flow = remember(scope, clock, timeInFuture) {
        flow {
            while (true) {
                val now = clock.now()
                val remainingDuration = (timeInFuture - now).absoluteValue
                emit(remainingDuration)

                val waitDuration = if (remainingDuration >= 30.minutes) {
                    val remainingSeconds = remainingDuration.inWholeSeconds
                    val toNextMinuteSeconds = remainingSeconds % 60
                    if (toNextMinuteSeconds == 0L) {
                        delay(2.seconds)
                        continue
                    } else {
                        toNextMinuteSeconds.seconds
                    }
                } else {
                    val remainingMillis = remainingDuration.inWholeMilliseconds
                    val toNextSecondMillis = remainingMillis % 1000

                    // Wait for the next whole second
                    if (toNextSecondMillis == 0L) {
                        delay(20.milliseconds)
                        continue
                    } else {
                        toNextSecondMillis.milliseconds
                    }
                }

                delay(waitDuration)
            }
        }.stateIn(scope, SharingStarted.Lazily, timeInFuture - clock.now())
    }

    return formatRelativeTimeUntilQuizTime(duration = flow.collectAsState().value)
}

@Composable
private fun formatRelativeTimeUntilQuizTime(duration: Duration): String {
    return if (duration > 30.minutes) {
        val minutes = duration.inWholeMinutes

        stringResource(id = R.string.quiz_participation_time_left_whole_minutes, minutes)
    } else if (duration > 1.minutes) {
        val minutes = duration.inWholeMinutes
        val seconds = (duration - minutes.minutes).inWholeSeconds

        stringResource(
            id = R.string.quiz_participation_time_left_minutes_and_seconds,
            minutes,
            seconds
        )
    } else {
        val seconds = duration.inWholeSeconds
        stringResource(id = R.string.quiz_participation_time_left_whole_seconds, seconds)
    }
}

private enum class JoinBatchErrorType {
    JOIN,
    START
}