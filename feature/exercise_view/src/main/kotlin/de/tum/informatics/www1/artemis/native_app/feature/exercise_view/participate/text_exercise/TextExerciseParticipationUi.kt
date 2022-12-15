package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

@Composable
internal fun TextExerciseParticipationUi(
    modifier: Modifier,
    text: String,
    syncState: SyncState,
    onUpdateText: (String) -> Unit,
    requestSubmit: () -> Unit
) {
    var displaySubmitDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Header(
            modifier = Modifier.fillMaxWidth(),
            onClickSubmit = {
                displaySubmitDialog = true
            },
            syncState = syncState
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxSize(),
            value = text,
            onValueChange = onUpdateText,
        )
    }

    if (displaySubmitDialog) {
        SubmitDialog(
            onSubmit = requestSubmit,
            dismissRequest = { displaySubmitDialog = false }
        )
    }
}

@Composable
private fun Header(modifier: Modifier, syncState: SyncState, onClickSubmit: () -> Unit) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        SyncStateUi(modifier = Modifier.weight(1f), syncState = syncState)

        Button(onClick = onClickSubmit) {
            Text(text = stringResource(id = R.string.participate_text_exercise_submit_button))
        }
    }
}

@Composable
private fun SyncStateUi(modifier: Modifier, syncState: SyncState) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val statusIconModifier = Modifier

        when (syncState) {
            is SyncState.SyncFailed -> {
                Icon(
                    modifier = statusIconModifier,
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = null
                )
            }

            SyncState.Synced -> {
                Icon(
                    modifier = statusIconModifier,
                    imageVector = Icons.Default.Done,
                    contentDescription = null
                )
            }

            SyncState.Syncing -> {
                CircularProgressIndicator(
                    modifier = statusIconModifier
                )
            }
        }

        val (textId, fontColor) = when (syncState) {
            is SyncState.SyncFailed -> R.string.participate_text_exercise_failed_sync to MaterialTheme.colorScheme.error
            SyncState.Synced -> R.string.participate_text_exercise_synced_changes to Color.Unspecified
            SyncState.Syncing -> R.string.participate_text_exercise_uploading_changes to Color.Unspecified
        }

        Text(
            text = stringResource(id = textId),
            color = fontColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SubmitDialog(onSubmit: () -> Unit, dismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismissRequest,
        title = { Text(text = stringResource(id = R.string.participate_text_exercise_submit_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.participate_text_exercise_submit_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text(text = stringResource(id = R.string.participate_text_exercise_submit_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = dismissRequest) {
                Text(text = stringResource(id = R.string.participate_text_exercise_submit_dialog_negative))
            }
        }
    )
}