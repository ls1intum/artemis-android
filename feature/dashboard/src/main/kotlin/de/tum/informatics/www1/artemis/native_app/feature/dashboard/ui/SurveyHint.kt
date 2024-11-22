package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.SurveyHintService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SURVEY_URL = "https://example.com/survey"     // TODO

@Composable
fun SurveyHint(
    modifier: Modifier = Modifier,
    surveyHintService: SurveyHintService
) {
    val shouldDisplaySurveyHint by surveyHintService.shouldShowSurveyHint.collectAsState(initial = false)
    var displaySurveyHint by rememberSaveable { mutableStateOf(false) }
    var showSurveyDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(shouldDisplaySurveyHint) {
        if (shouldDisplaySurveyHint) displaySurveyHint = true
    }

    AnimatedVisibility(displaySurveyHint) {
        SurveyHintImpl(
            modifier = modifier,
            onClick = { showSurveyDialog = true }
        )
    }

    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    if (showSurveyDialog) {
        SurveyDialog(
            onClose = { participate ->
                if (participate) {
                    scope.launch {
                        uriHandler.openUri(SURVEY_URL)
                        surveyHintService.dismissSurveyHintPermanently()

                        delay(2000) // Wait for the survey to open before hiding the hint
                        displaySurveyHint = false
                    }
                }

                showSurveyDialog = false
            }
        )
    }
}

@Composable
private fun SurveyHintImpl(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Survey available",
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun SurveyDialog(
    modifier: Modifier = Modifier,
    onClose: (participate: Boolean) -> Unit
) {

    AlertDialog(
        onDismissRequest = { onClose(false) },
        title = { Text(text = "We need your help!") },
        text = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(text = "Do you have 5-10 minutes to take a short survey and help us improve the app?")

                Icon(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.CenterHorizontally)
                    ,
                    imageVector = Icons.Outlined.Feedback,
                    contentDescription = null,
                )

                Text("Thank you for your consideration!")
            }
        },
        confirmButton = {
            Button(
                onClick = { onClose(true) }
            ) {
                Text(text = "Participate")

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onClose(false) }
            ) {
                Text(text = "Not now")
            }
        }
    )
}

@Preview
@Composable
fun SurveyHintPreview() {
    SurveyHintImpl(
        modifier = Modifier,
        onClick = {}
    )
}

@Preview
@Composable
fun SurveyDialogPreview() {
    SurveyDialog(onClose = {})
}