package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.ConnectionStatusUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.getFormattedRelativeToFutureTimeQuizStyle
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.Footer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
internal fun WorkOnQuizQuestionFooter(
    modifier: Modifier,
    quizType: QuizType.WorkableQuizType,
    lastSubmissionTime: Instant?,
    endDate: Instant?,
    isConnected: Boolean,
    latestWebsocketSubmission: Result<QuizSubmission>?,
    clock: Clock,
    canNavigateToPreviousQuestion: Boolean,
    canNavigateToNextQuestion: Boolean,
    onRequestPreviousQuestion: () -> Unit,
    onRequestNextQuestion: () -> Unit,
    onRequestRetrySave: () -> Unit
) {
    var displayButtonText by remember { mutableStateOf(true) }
    val onTextLayout = { result: TextLayoutResult ->
        if (result.hasVisualOverflow) displayButtonText = false
    }

    val buttonModifier = if (displayButtonText) Modifier.fillMaxWidth() else Modifier

    if (!isConnected) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ConnectionStatusUi(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center),
                isConnected = false
            )
        }
    }

    if (latestWebsocketSubmission != null && latestWebsocketSubmission.isFailure) {
        // Display ui that the latest submission could not be uploaded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.quiz_participation_save_failed),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Button(onClick = onRequestRetrySave) {
                    Text(text = stringResource(id = R.string.quiz_participation_save_failed_try_again_button))
                }
            }
        }
    }

    Footer(
        modifier = modifier,
        boxTwoWeight = 0.5f,
        boxOne = {
            Button(
                onClick = onRequestPreviousQuestion,
                modifier = buttonModifier.align(Alignment.Center),
                enabled = canNavigateToPreviousQuestion
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)

                if (displayButtonText) {
                    Text(
                        text = stringResource(id = R.string.quiz_participation_button_previous_question),
                        onTextLayout = onTextLayout,
                        maxLines = 1
                    )
                }
            }
        },
        boxTwo = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (endDate != null) {
                    val isQuizDone = endDate.hasPassed()
                    val relativeTime = getFormattedRelativeToFutureTimeQuizStyle(endDate, clock)

                    val text = if (isQuizDone) {
                        stringResource(id = R.string.quiz_participation_time_left_quiz_ended)
                    } else {
                        stringResource(id = R.string.quiz_participation_time_left, relativeTime)
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                val submissionText = when(quizType) {
                    QuizType.Live -> {
                        if (lastSubmissionTime != null) {
                            val relLastSubmissionTime =
                                getRelativeTime(to = lastSubmissionTime, clock = clock)
                            stringResource(
                                id = R.string.quiz_participation_last_saved,
                                relLastSubmissionTime
                            )
                        } else {
                            stringResource(id = R.string.quiz_participation_never_saved)
                        }
                    }
                    QuizType.Practice -> stringResource(id = R.string.quiz_participation_practice_mode)
                }

                Text(
                    text = submissionText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        boxThree = {
            Button(
                onClick = onRequestNextQuestion,
                modifier = buttonModifier.align(Alignment.Center),
                enabled = canNavigateToNextQuestion
            ) {
                if (displayButtonText) {
                    Text(
                        text = stringResource(id = R.string.quiz_participation_button_next_question),
                        onTextLayout = onTextLayout,
                        maxLines = 1
                    )
                }

                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    )
}