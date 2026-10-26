package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.getProblemStatementWebViewState

@Composable
internal fun TextExerciseParticipationScreen(
    modifier: Modifier,
    viewModel: TextExerciseParticipationViewModel,
    exercise: Exercise,
    onNavigateUp: () -> Unit
) {
    val context by LocalArtemisContextProvider.current.collectArtemisContextAsState()
    val state by viewModel.state.collectAsState()
    val currentExercise = state.participation?.exercise ?: exercise
    var reviewConflict by rememberSaveable { mutableStateOf(false) }
    var assignmentSelected by rememberSaveable { mutableStateOf(false) }
    var webView: WebView? by remember { mutableStateOf(null) }
    val webViewState = getProblemStatementWebViewState(
        serverUrl = context.serverUrl,
        courseId = currentExercise.course?.id,
        exerciseId = currentExercise.id ?: 0L,
        participationId = null
    )
    val assignment = @Composable { childModifier: Modifier ->
        if (webViewState != null) ArtemisWebView(
            modifier = childModifier, webViewState = webViewState, webView = webView, setWebView = { webView = it }
        ) else Box(childModifier.padding(16.dp)) {
            SelectionContainer { Text(currentExercise.problemStatement.orEmpty()) }
        }
    }
    val editor = @Composable { childModifier: Modifier ->
        TextExerciseParticipationUi(
            modifier = childModifier, state = state, onUpdateText = viewModel::updateText,
            requestSubmit = viewModel::submit, retryLoad = viewModel::onRequestReload,
            retrySave = viewModel::retryLocalSave, reviewConflict = { reviewConflict = true }
        )
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            ArtemisTopAppBar(
                navigationIcon = { NavigationBackButton(onNavigateUp) },
                title = { Text(currentExercise.title ?: stringResource(R.string.participate_text_exercise_title), maxLines = 1) }
            )
        }
    ) { padding ->
        if (state.sessionExpired) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.participate_text_exercise_session_changed))
            }
        } else {
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                if (maxWidth >= 840.dp) {
                    Row(Modifier.fillMaxSize()) {
                        Column(Modifier.weight(0.42f)) {
                            Text(
                                stringResource(R.string.participate_text_exercise_tab_problem_statement),
                                modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium
                            )
                            assignment(Modifier.fillMaxSize())
                        }
                        VerticalDivider()
                        editor(Modifier.weight(0.58f).fillMaxSize())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        TabRow(selectedTabIndex = if (assignmentSelected) 1 else 0) {
                            Tab(selected = !assignmentSelected, onClick = { assignmentSelected = false }, text = {
                                Text(stringResource(R.string.participate_text_exercise_tab_submission))
                            })
                            Tab(selected = assignmentSelected, onClick = { assignmentSelected = true }, text = {
                                Text(stringResource(R.string.participate_text_exercise_tab_problem_statement))
                            })
                        }
                        if (assignmentSelected) assignment(Modifier.fillMaxSize()) else editor(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
    if (reviewConflict && state.conflict != null && !state.sessionExpired) {
        AlertDialog(
            onDismissRequest = { reviewConflict = false },
            title = { Text(stringResource(R.string.participate_text_exercise_conflict_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.participate_text_exercise_conflict_detail))
                    SelectionContainer { Text(state.conflict?.text.orEmpty(), modifier = Modifier.padding(top = 16.dp)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.keepLocalDraft(); reviewConflict = false }) {
                    Text(stringResource(R.string.participate_text_exercise_keep_draft))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.useServerVersion(); reviewConflict = false }) {
                    Text(stringResource(R.string.participate_text_exercise_use_server))
                }
            }
        )
    }
}
