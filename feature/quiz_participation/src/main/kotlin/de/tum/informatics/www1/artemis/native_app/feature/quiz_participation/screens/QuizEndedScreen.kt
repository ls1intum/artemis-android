package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R

@Composable
internal fun QuizEndedScreen(modifier: Modifier, onRequestLeave: () -> Unit) {
    Box(modifier = modifier) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.quiz_participation_quiz_ended_text),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Button(
                modifier = Modifier,
                onClick = onRequestLeave
            ) {
                Text(text = stringResource(id = R.string.quiz_participation_quiz_ended_leave_button))
            }
        }
    }
}