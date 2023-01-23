package de.tum.informatics.www1.artemis.native_app.feature.quiz.participation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizEndedScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.WaitForQuizStartScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.WorkOnQuizQuestionsScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.androidx.compose.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun QuizParticipationUi(
    modifier: Modifier,
    viewModel: QuizParticipationViewModel,
    onNavigateUp: () -> Unit
) {
    val accountService: AccountService = get()
    val authToken by accountService.authToken.collectAsState(initial = "")

    val serverConfigurationService: ServerConfigurationService = get()
    val serverUrl: String =
        serverConfigurationService.serverUrl.collectAsState(initial = "").value.dropLast(1)

    val exerciseDataState by viewModel.quizExerciseDataState.collectAsState()
    val isWaitingForQuizStart by viewModel.waitingForQuizStart.collectAsState(initial = false)
    val hasQuizEnded by viewModel.quizEndedStatus.collectAsState(initial = false)
    val isConnected by viewModel.isConnected.collectAsState(initial = false)
    val batch by viewModel.quizBatch.collectAsState(initial = null)

    val serverClock by viewModel.serverClock.collectAsState(initial = Clock.System)

    var joinBatchError: JoinBatchErrorType? by rememberSaveable { mutableStateOf(null) }

    var startBatchJob: Job? by remember { mutableStateOf(null) }
    var joinBatchJob: Job? by remember { mutableStateOf(null) }

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
                onRequestRefresh = {
                    viewModel.retryLoadExercise()
                    viewModel.reconnectWebsocket()
                },
                onClickStartQuiz = {
                    startBatchJob = viewModel.startBatch { successful ->
                        if (!successful) {
                            joinBatchError = JoinBatchErrorType.START
                        }
                        startBatchJob = null
                    }
                },
                onClickJoinBatch = { passcode ->
                    joinBatchJob = viewModel.joinBatch(passcode) { successful ->
                        if (!successful) {
                            joinBatchError = JoinBatchErrorType.JOIN
                        }
                        joinBatchJob = null
                    }
                }
            )
        } else if (hasQuizEnded) {
            QuizEndedScreen(
                modifier = Modifier.fillMaxSize(),
                onRequestLeave = onNavigateUp
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

    if (joinBatchJob != null) {
        BatchJobDialog(
            message = stringResource(id = R.string.quiz_participation_wait_for_start_join_batch_dialog_message),
            confirmText = stringResource(id = R.string.quiz_participation_wait_for_start_join_batch_dialog_negative),
            onDismiss = {
                joinBatchJob?.cancel()
                joinBatchJob = null
            }
        )
    }

    if (startBatchJob != null) {
        BatchJobDialog(
            message = stringResource(id = R.string.quiz_participation_wait_for_start_start_batch_dialog_message),
            confirmText = stringResource(id = R.string.quiz_participation_wait_for_start_start_batch_dialog_negative),
            onDismiss = {
                startBatchJob?.cancel()
                startBatchJob = null
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