package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.isInitializationAfterDueDate
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.date.isInFuture
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun TextExerciseParticipationScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    participationId: Long,
    onNavigateUp: () -> Unit
) {
    EmptyDataStateUi(dataState = viewModel.exercise.collectAsState().value) { exercise ->
        val exerciseId: Long = exercise.id

        val participationViewModel: TextExerciseParticipationViewModel =
            koinViewModel { parametersOf(exerciseId, participationId) }

        val latestResult by participationViewModel.latestResult.collectAsState()
        val latestSubmission by participationViewModel.latestSubmission.collectAsState()
        val participation = participationViewModel.initialParticipation.collectAsState().value

        var displayDiscardChangesDialog by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.participate_text_exercise_title),
                        )
                    }
                )
            }
        ) { padding ->
            if (latestSubmission == null || participation == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                TextExerciseParticipationUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                    text = participationViewModel.text.collectAsState("").value,
                    syncState = participationViewModel.syncState.collectAsState(SyncState.Syncing).value,
                    isActive = isActive(
                        exercise = exercise,
                        latestResult = latestResult,
                        participation = participation
                    ),
                    onUpdateText = participationViewModel::updateText,
                    submission = latestSubmission,
                    requestSubmit = participationViewModel::retrySync
                )
            }
        }

        if (displayDiscardChangesDialog) {
            DiscardChangesDialog(
                onDiscardChanges = onNavigateUp,
                dismissRequest = { displayDiscardChangesDialog = false }
            )
        }
    }
}

private fun isAlwaysActive(
    latestResult: Result?,
    exercise: Exercise,
    participation: Participation
): Boolean {
    return latestResult == null &&
            (exercise.dueDate == null || participation.isInitializationAfterDueDate(exercise.dueDate))
}

@Composable
private fun isActive(
    latestResult: Result?,
    exercise: Exercise,
    participation: Participation
): Boolean {
    return latestResult == null && (
            isAlwaysActive(null, exercise, participation) || (
                    exercise.dueDate != null
                            && exercise.getDueDate(participation)?.isInFuture() ?: true
                    )
            )
}

@Composable
private fun DiscardChangesDialog(onDiscardChanges: () -> Unit, dismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismissRequest,
        title = { Text(text = stringResource(id = R.string.participate_text_exercise_discard_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.participate_text_exercise_discard_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onDiscardChanges) {
                Text(text = stringResource(id = R.string.participate_text_exercise_discard_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = dismissRequest) {
                Text(text = stringResource(id = R.string.participate_text_exercise_discard_dialog_negative))
            }
        }
    )
}