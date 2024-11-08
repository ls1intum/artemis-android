package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ButtonWithLoadingAnimation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.ConnectionStatusUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.getFormattedRelativeToFutureTimeQuizStyle
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

internal const val TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN = "TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN"
internal const val TEST_TAG_TEXT_FIELD_BATCH_PASSWORD = "TEST_TAG_TEXT_FIELD_BATCH_PASSWORD"

/**
 * @param isStartingOrJoiningQuiz if the user is currently requesting to start their quiz run or joining a batch. True while this requests is firing.
 */
@Composable
internal fun WaitForQuizStartScreen(
    modifier: Modifier,
    exercise: QuizExercise,
    isConnected: Boolean,
    isStartingOrJoiningQuiz: Boolean,
    batch: QuizExercise.QuizBatch?,
    clock: Clock,
    onRequestRefresh: () -> Unit,
    onClickJoinBatch: (passcode: String) -> Unit,
    onClickStartQuiz: () -> Unit
) {
    val waitingStatus: WaitingStatus = remember(batch, exercise) {
        when {
            batch != null || exercise.quizMode == QuizExercise.QuizMode.SYNCHRONIZED ->
                WaitingStatus.Synchronized(batch?.startTime)

            exercise.quizMode == QuizExercise.QuizMode.BATCHED && (exercise.remainingNumberOfAttempts
                ?: 1) > 0 ->
                WaitingStatus.JoinBatched

            exercise.quizMode == QuizExercise.QuizMode.INDIVIDUAL && (exercise.remainingNumberOfAttempts
                ?: 1) > 0 ->
                WaitingStatus.StartNow

            else -> WaitingStatus.NoMoreAttempts(exercise.allowedNumberOfAttempts ?: 1)
        }
    }

    WaitForQuizStartScreen(
        modifier = modifier,
        waitingStatus = waitingStatus,
        isStartingOrJoiningQuiz = isStartingOrJoiningQuiz,
        isConnected = isConnected,
        clock = clock,
        onRequestRefresh = onRequestRefresh,
        onClickStartQuiz = onClickStartQuiz,
        onClickJoinBatch = onClickJoinBatch
    )
}

@Composable
private fun WaitForQuizStartScreen(
    modifier: Modifier,
    waitingStatus: WaitingStatus,
    isStartingOrJoiningQuiz: Boolean,
    isConnected: Boolean,
    clock: Clock,
    onRequestRefresh: () -> Unit,
    onClickJoinBatch: (passcode: String) -> Unit,
    onClickStartQuiz: () -> Unit
) {
    Column(modifier = modifier.testTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val bodyModifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(8.dp)

            when (waitingStatus) {
                is WaitingStatus.JoinBatched -> BodyJoinBatched(
                    modifier = bodyModifier,
                    isJoining = isStartingOrJoiningQuiz,
                    onClickJoinBatch = onClickJoinBatch
                )

                is WaitingStatus.NoMoreAttempts -> BodyNoMoreAttempts(
                    modifier = bodyModifier,
                    waitingStatus = waitingStatus
                )

                is WaitingStatus.StartNow -> BodyStartNow(
                    modifier = bodyModifier,
                    isStarting = isStartingOrJoiningQuiz,
                    onClickStartNow = onClickStartQuiz
                )

                is WaitingStatus.Synchronized -> BodySynchronized(
                    modifier = bodyModifier,
                    waitingStatus = waitingStatus,
                    clock = clock
                )
            }
        }

        Footer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            boxOne = {
                ConnectionStatusUi(
                    modifier = Modifier.align(Alignment.Center),
                    isConnected = isConnected
                )
            },
            boxTwo = {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = onRequestRefresh
                ) {
                    Text(text = stringResource(id = R.string.quiz_participation_wait_for_start_refresh_button))
                }
            },
            boxThree = {}
        )
    }
}

@Composable
private fun BodySynchronized(
    modifier: Modifier,
    waitingStatus: WaitingStatus.Synchronized,
    clock: Clock
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_please_wait),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_explanation),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        if (waitingStatus.startDate != null) {
            Spacer(modifier = Modifier.height(8.dp))

            val timeUntilStart =
                getFormattedRelativeToFutureTimeQuizStyle(waitingStatus.startDate, clock)

            Text(
                text = stringResource(id = R.string.quiz_participation_wait_for_start_time_until_start),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = timeUntilStart,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun BodyJoinBatched(
    modifier: Modifier,
    isJoining: Boolean,
    onClickJoinBatch: (passcode: String) -> Unit
) {
    var passcode by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_enter_password),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .widthIn(max = 600.dp)
                .testTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD),
            value = passcode,
            onValueChange = { passcode = it },
            singleLine = true,
        )

        StartButton(
            isStarting = isJoining,
            text = stringResource(id = R.string.quiz_participation_wait_for_start_join_button),
            onClick = { onClickJoinBatch(passcode) }
        )
    }
}

@Composable
private fun BodyStartNow(
    modifier: Modifier,
    isStarting: Boolean,
    onClickStartNow: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_start_now),
            style = MaterialTheme.typography.headlineLarge
        )

        StartButton(
            isStarting = isStarting,
            onClick = onClickStartNow,
            text = stringResource(id = R.string.quiz_participation_wait_for_start_start_now_button)
        )
    }
}

@Composable
private fun BodyNoMoreAttempts(modifier: Modifier, waitingStatus: WaitingStatus.NoMoreAttempts) {
    val textRes = when (waitingStatus.allowedAttempts) {
        1 -> R.string.quiz_participation_wait_for_start_no_more_attempts_already_participated
        else -> R.string.quiz_participation_wait_for_start_no_more_attempts_no_more_attempts
    }

    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StartButton(
    modifier: Modifier = Modifier,
    isStarting: Boolean,
    text: String,
    onClick: () -> Unit
) {
    ButtonWithLoadingAnimation(
        modifier = modifier,
        isLoading = isStarting,
        onClick = onClick
    ) {
        Text(text = text)
    }
}

private sealed interface WaitingStatus {
    data class Synchronized(val startDate: Instant?) : WaitingStatus
    object JoinBatched : WaitingStatus
    object StartNow : WaitingStatus
    data class NoMoreAttempts(val allowedAttempts: Int) : WaitingStatus
}

private class WaitingStatusProvider : PreviewParameterProvider<WaitingStatus> {
    override val values: Sequence<WaitingStatus>
        get() = sequenceOf(
            WaitingStatus.Synchronized(null),
            WaitingStatus.Synchronized(Clock.System.now() + 5.minutes),
            WaitingStatus.JoinBatched,
            WaitingStatus.StartNow,
            WaitingStatus.NoMoreAttempts(1),
            WaitingStatus.NoMoreAttempts(5),
        )
}

@Preview
@Composable
private fun WaitForQuizStartScreenPreview(
    @PreviewParameter(WaitingStatusProvider::class) waitingStatus: WaitingStatus
) {
    Surface {
        WaitForQuizStartScreen(
            modifier = Modifier.fillMaxSize(),
            waitingStatus = waitingStatus,
            isConnected = true,
            clock = Clock.System,
            onRequestRefresh = {},
            onClickStartQuiz = {},
            onClickJoinBatch = {},
            isStartingOrJoiningQuiz = false
        )
    }
}