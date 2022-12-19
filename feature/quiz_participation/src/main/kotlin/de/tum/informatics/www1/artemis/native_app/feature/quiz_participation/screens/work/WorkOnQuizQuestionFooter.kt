package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.ConnectionStatusUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.Footer
import kotlinx.datetime.Instant

@Composable
internal fun WorkOnQuizQuestionFooter(
    modifier: Modifier,
    lastSubmissionTime: Instant?,
    endDate: Instant?,
    canNavigateToPreviousQuestion: Boolean,
    canNavigateToNextQuestion: Boolean,
    onRequestPreviousQuestion: () -> Unit,
    onRequestNextQuestion: () -> Unit
) {
    var displayButtonText by remember { mutableStateOf(true) }
    val onTextLayout = { result: TextLayoutResult ->
        if (result.hasVisualOverflow) displayButtonText = false
    }

    val buttonModifier = if (displayButtonText) Modifier.fillMaxWidth() else Modifier

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
                    val relativeTime = getRelativeTime(to = endDate)

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

                val submissionText = if (lastSubmissionTime != null) {
                    val relLastSubmissionTime = getRelativeTime(to = lastSubmissionTime)
                    stringResource(
                        id = R.string.quiz_participation_last_saved,
                        relLastSubmissionTime
                    )
                } else {
                    stringResource(id = R.string.quiz_participation_never_saved)
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