package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.isInitializationAfterDueDate
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.date.isInFuture
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ProblemStatementWebView
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.courseId
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.getProblemStatementWebViewState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun TextExerciseParticipationScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    participationId: Long,
    onNavigateUp: () -> Unit
) {
    val exerciseDataState by viewModel.exerciseDataState.collectAsState()
    val courseId: Long? = exerciseDataState.courseId

    EmptyDataStateUi(dataState = exerciseDataState) { exercise ->
        val exerciseId: Long = exercise.id

        val participationViewModel: TextExerciseParticipationViewModel =
            koinViewModel { parametersOf(exerciseId, participationId) }

        val latestResult by participationViewModel.latestResult.collectAsState()
        val latestSubmission by participationViewModel.latestSubmission.collectAsState()
        val participation = participationViewModel.initialParticipation.collectAsState().value

        var displayDiscardChangesDialog by rememberSaveable { mutableStateOf(false) }

        val displayProblemStatementOnSide = getWindowSizeClass()
            .widthSizeClass >= WindowWidthSizeClass.Medium

        val webViewState: WebViewState? = getProblemStatementWebViewState(
            serverUrl = viewModel.serverUrl.collectAsState().value,
            courseId = courseId,
            exerciseId = exerciseId
        )

        var webView: WebView? by remember { mutableStateOf(null) }

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
                val participationUi = @Composable { modifier: Modifier ->
                    TextExerciseParticipationUi(
                        modifier = modifier,
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

                val problemStatementUi = @Composable { modifier: Modifier ->
                    if (webViewState != null) {
                        ProblemStatementWebView(
                            modifier = modifier,
                            webViewState = webViewState,
                            webView = webView,
                            setWebView = { webView = it }
                        )
                    } else {
                        Box(modifier = modifier)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (displayProblemStatementOnSide) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            participationUi(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            )

                            problemStatementUi(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            )
                        }
                    } else {
                        val childModifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)

                        TabView(
                            modifier = Modifier.fillMaxSize(),
                            submissionContent = {
                                participationUi(childModifier)
                            },
                            problemStatementContent = {
                                problemStatementUi(childModifier)
                            }
                        )
                    }
                }
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

@Composable
private fun TabView(
    modifier: Modifier,
    submissionContent: @Composable () -> Unit,
    problemStatementContent: @Composable () -> Unit
) {
    var selectedTab: Tab by rememberSaveable { mutableStateOf(Tab.SUBMISSION) }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab.index) {
            Tab.values().forEach { tab ->
                Tab(
                    text = { Text(text = stringResource(id = tab.title)) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }

        when (selectedTab) {
            Tab.SUBMISSION -> submissionContent()
            Tab.PROBLEM_STATEMENT -> problemStatementContent()
        }
    }
}

private enum class Tab(@StringRes val title: Int, val icon: ImageVector, val index: Int) {
    SUBMISSION(R.string.participate_text_exercise_tab_submission, Icons.Default.EditNote, 0),
    PROBLEM_STATEMENT(R.string.participate_text_exercise_tab_problem_statement, Icons.Default.ListAlt, 1)
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