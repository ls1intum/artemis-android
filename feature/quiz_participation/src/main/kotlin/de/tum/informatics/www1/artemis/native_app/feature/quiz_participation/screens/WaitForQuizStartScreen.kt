package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.ConnectionStatusUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.quizParticipationModule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun WaitForQuizStartScreen(
    modifier: Modifier,
    exercise: QuizExercise,
    isConnected: Boolean,
    batch: QuizExercise.QuizBatch?,
    onRequestRefresh: () -> Unit
) {
    val waitingStatus: WaitingStatus = when {
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

    WaitForQuizStartScreen(
        modifier = modifier,
        waitingStatus = waitingStatus,
        isConnected = isConnected,
        onRequestRefresh = onRequestRefresh
    )
}

@Composable
private fun WaitForQuizStartScreen(
    modifier: Modifier,
    waitingStatus: WaitingStatus,
    isConnected: Boolean,
    onRequestRefresh: () -> Unit
) {
    Column(modifier = modifier) {
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
                    waitingStatus = waitingStatus
                )

                is WaitingStatus.NoMoreAttempts -> BodyNoMoreAttempts(
                    modifier = bodyModifier,
                    waitingStatus = waitingStatus
                )

                is WaitingStatus.StartNow -> BodyStartNow(
                    modifier = bodyModifier,
                    waitingStatus = waitingStatus
                )

                is WaitingStatus.Synchronized -> BodySynchronized(
                    modifier = bodyModifier,
                    waitingStatus = waitingStatus
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 8.dp)
        ) {
            val weightOne = Modifier
                .weight(1f)
                .fillMaxHeight()
            Box(modifier = weightOne) {
                ConnectionStatusUi(
                    modifier = Modifier.align(Alignment.Center),
                    isConnected = isConnected
                )
            }

            Box(modifier = weightOne) {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = onRequestRefresh
                ) {
                    Text(text = stringResource(id = R.string.quiz_participation_wait_for_start_refresh_button))
                }
            }

            Box(modifier = weightOne)
        }
    }
}

@Composable
private fun BodySynchronized(modifier: Modifier, waitingStatus: WaitingStatus.Synchronized) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_please_wait),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(id = R.string.quiz_participation_wait_for_start_explanation),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        if (waitingStatus.startDate != null) {
            val timeUntilStart = getRelativeTime(to = waitingStatus.startDate)

            Text(
                text = stringResource(
                    id = R.string.quiz_participation_wait_for_start_time_until_start,
                    timeUntilStart
                ),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BodyJoinBatched(modifier: Modifier, waitingStatus: WaitingStatus.JoinBatched) {

}

@Composable
private fun BodyStartNow(modifier: Modifier, waitingStatus: WaitingStatus.StartNow) {

}

@Composable
private fun BodyNoMoreAttempts(modifier: Modifier, waitingStatus: WaitingStatus.NoMoreAttempts) {

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
    WaitForQuizStartScreen(
        modifier = Modifier.fillMaxSize(),
        waitingStatus = waitingStatus,
        isConnected = true,
        onRequestRefresh = {}
    )
}