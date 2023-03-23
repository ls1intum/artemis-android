package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

@Composable
internal fun ViewResultScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    onCloseResult: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.result_view_title)) },
                navigationIcon = {
                    IconButton(onClick = onCloseResult) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        val exerciseDataState = viewModel.exerciseDataState.collectAsState().value

        val latestResultDataState by viewModel.latestResultDataState.collectAsState()

        val latestIndividualDueDate by viewModel.latestIndividualDueDate.collectAsState()

        val feedbackItems by viewModel.feedbackItems.collectAsState()

        val buildLogs by viewModel.buildLogs.collectAsState()

        ExerciseDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            value = latestResultDataState join exerciseDataState,
            onClickRetry = { viewModel.requestReloadExercise() },
            onSuccess = { (latestResult, exercise) ->
                ResultDetailUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    exercise = exercise,
                    latestResult = latestResult ?: return@ExerciseDataStateUi,
                    feedbackItems = feedbackItems.orElse(emptyList()),
                    latestIndividualDueDate = latestIndividualDueDate.orElse(null),
                    buildLogs = buildLogs.orElse(emptyList())
                )
            }
        )
    }
}