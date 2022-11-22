package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseViewModule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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
        val exercise = viewModel.exercise.collectAsState(initial = DataState.Loading()).value

        val latestResultDataState =
            viewModel.latestResult.collectAsState(initial = DataState.Loading()).value

        val latestIndividualDueDate =
            viewModel.latestIndividualDueDate.collectAsState(initial = DataState.Loading()).value

        val feedbackItems =
            viewModel.feedbackItems.collectAsState(initial = DataState.Loading()).value

        val buildLogs = viewModel.buildLogs.collectAsState(initial = DataState.Loading()).value

        ExerciseDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            value = latestResultDataState,
            viewModel = viewModel
        ) { latestResult ->
            if (exercise !is DataState.Success) return@ExerciseDataStateUi

            ResultDetailUi(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                exercise = exercise.data,
                latestResult = latestResult ?: return@ExerciseDataStateUi,
                feedbackItems = feedbackItems.orElse(emptyList()),
                latestIndividualDueDate = latestIndividualDueDate.orElse(null),
                buildLogs = buildLogs.orElse(emptyList())
            )
        }
    }
}