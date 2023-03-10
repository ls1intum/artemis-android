package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

sealed interface QuizEndedScreenType {
    data class Live(val onRequestLeave: () -> Unit) : QuizEndedScreenType

    data class Practice(
        val onRequestSubmit: () -> Unit,
        val isSubmitting: Boolean
    ) : QuizEndedScreenType
}

@Composable
internal fun QuizEndedScreen(modifier: Modifier, type: QuizEndedScreenType) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (text, buttonText) = when (type) {
                is QuizEndedScreenType.Live ->
                    R.string.quiz_participation_quiz_ended_live_text to R.string.quiz_participation_quiz_ended_leave_button
                is QuizEndedScreenType.Practice ->
                    R.string.quiz_participation_quiz_ended_practice_text to R.string.quiz_participation_quiz_ended_submit_button
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = text),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            val isSubmitting = type is QuizEndedScreenType.Practice && type.isSubmitting

            Button(
                modifier = Modifier,
                enabled = !isSubmitting,
                onClick = when (type) {
                    is QuizEndedScreenType.Live -> type.onRequestLeave
                    is QuizEndedScreenType.Practice -> type.onRequestSubmit
                }
            ) {
                Text(text = stringResource(id = buttonText))
            }

            if (isSubmitting) {
                CircularProgressIndicator()
            }
        }
    }
}