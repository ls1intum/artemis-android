package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

@Composable
internal fun TextExerciseParticipationUi(
    modifier: Modifier,
    text: String,
    syncState: SyncState,
    isActive: Boolean,
    submission: TextSubmission?,
    onUpdateText: (String) -> Unit,
    requestSubmit: () -> Unit
) {

    Column(modifier = modifier) {
        Header(
            modifier = Modifier.fillMaxWidth(),
            onClickSubmit = requestSubmit,
            syncState = syncState
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxSize(),
            value = text,
            onValueChange = onUpdateText,
            enabled = isActive && submission != null
        )
    }
}

@Composable
private fun Header(modifier: Modifier, syncState: SyncState, onClickSubmit: () -> Unit) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        SyncStateUi(modifier = Modifier.weight(1f), syncState = syncState)

        Button(onClick = onClickSubmit, enabled = syncState !is SyncState.Synced) {
            Text(text = stringResource(id = R.string.participate_text_exercise_submit_button))
        }
    }
}

@Composable
private fun SyncStateUi(modifier: Modifier, syncState: SyncState) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val statusIconModifier = Modifier

        when (syncState) {
            SyncState.SyncFailed -> {
                Icon(
                    modifier = statusIconModifier,
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = null
                )
            }

            is SyncState.Synced -> {
                Icon(
                    modifier = statusIconModifier,
                    imageVector = Icons.Default.Done,
                    contentDescription = null
                )
            }

            SyncState.SyncPending -> {
                Icon(
                    modifier = statusIconModifier,
                    imageVector = Icons.Outlined.Pending,
                    contentDescription = null
                )
            }

            SyncState.Syncing -> {
                CircularProgressIndicator(
                    modifier = statusIconModifier
                )
            }
        }

        if (syncState != SyncState.SyncPending) {
            val (textId, fontColor) = when (syncState) {
                SyncState.SyncFailed -> R.string.participate_text_exercise_failed_sync to MaterialTheme.colorScheme.error
                is SyncState.Synced -> R.string.participate_text_exercise_synced_changes to Color.Unspecified
                SyncState.Syncing -> R.string.participate_text_exercise_uploading_changes to Color.Unspecified
                else -> 0 to Color.Unspecified
            }

            Text(
                text = stringResource(id = textId),
                style = MaterialTheme.typography.labelMedium,
                color = fontColor
            )
        }
    }
}
