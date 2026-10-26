package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R

internal const val TEST_TAG_TEXT_FIELD_PARTICIPATION = "text field participation"

@Composable
internal fun TextExerciseParticipationUi(
    modifier: Modifier,
    state: TextExerciseEditorState,
    onUpdateText: (String) -> Unit,
    requestSubmit: () -> Unit,
    retryLoad: () -> Unit,
    retrySave: () -> Unit,
    reviewConflict: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DraftStatus(state)
                Text(
                    text = stringResource(R.string.participate_text_exercise_local_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = requestSubmit, enabled = state.canSubmit) {
                Text(stringResource(R.string.participate_text_exercise_submit_button))
            }
        }
        if (state.closed) {
            Text(stringResource(R.string.participate_text_exercise_closed), style = MaterialTheme.typography.bodyMedium)
        }
        if (state.localSaveFailed) {
            Notice(
                stringResource(R.string.participate_text_exercise_local_failed),
                stringResource(R.string.participate_text_exercise_retry), retrySave
            )
        }
        if (state.loadFailed) {
            Notice(
                stringResource(R.string.participate_text_exercise_load_failed),
                stringResource(R.string.participate_text_exercise_reload), retryLoad
            )
        }
        if (state.submitFailed) {
            Notice(
                stringResource(R.string.participate_text_exercise_failed_sync),
                stringResource(R.string.participate_text_exercise_reload), retryLoad
            )
        }
        if (state.conflict != null) {
            Notice(
                stringResource(R.string.participate_text_exercise_conflict),
                stringResource(R.string.participate_text_exercise_review), reviewConflict
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag(TEST_TAG_TEXT_FIELD_PARTICIPATION),
            value = state.text,
            onValueChange = onUpdateText,
            enabled = state.draftReady,
            label = { Text(stringResource(R.string.participate_text_exercise_answer)) },
            placeholder = { Text(stringResource(R.string.participate_text_exercise_placeholder)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            isError = state.text.length > TEXT_EXERCISE_MAX_LENGTH
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { clipboard.setText(AnnotatedString(state.text)) }, enabled = state.text.isNotEmpty()) {
                Text(stringResource(R.string.participate_text_exercise_copy))
            }
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.participate_text_exercise_count, state.text.length, TEXT_EXERCISE_MAX_LENGTH),
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                color = if (state.text.length > TEXT_EXERCISE_MAX_LENGTH) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.text.length > TEXT_EXERCISE_MAX_LENGTH) {
            Text(stringResource(R.string.participate_text_exercise_too_long), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DraftStatus(state: TextExerciseEditorState) {
    val message = when {
        state.submittingElsewhere -> R.string.participate_text_exercise_other_submission
        state.submitting -> R.string.participate_text_exercise_uploading_changes
        state.localSaveFailed -> R.string.participate_text_exercise_unsaved
        state.saving -> R.string.participate_text_exercise_saving
        state.loading -> R.string.participate_text_exercise_loading
        state.isSubmitted -> R.string.participate_text_exercise_synced_changes
        state.recovered -> R.string.participate_text_exercise_recovered
        else -> R.string.participate_text_exercise_saved_locally
    }
    Row(
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.submitting || state.saving || state.loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = when {
                    state.localSaveFailed -> Icons.Default.Warning
                    state.isSubmitted -> Icons.Default.CheckCircle
                    else -> Icons.Default.PhoneAndroid
                },
                contentDescription = null,
                tint = if (state.localSaveFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Text(stringResource(message), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Notice(text: String, action: String, onAction: () -> Unit) {
    Column {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onAction) { Text(action) }
    }
}
