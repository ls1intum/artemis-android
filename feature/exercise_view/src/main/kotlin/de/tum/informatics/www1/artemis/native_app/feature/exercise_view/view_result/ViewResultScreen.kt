package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result

import android.webkit.WebView
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.latestParticipation
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.LocalTemplateStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.computeTemplateStatus
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.getFeedbackViewWebViewState

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
        val serverUrl by viewModel.serverUrl.collectAsState()
        val authToken by viewModel.authToken.collectAsState()
        val exerciseDataState by viewModel.exerciseDataState.collectAsState()
        val latestResultDataState by viewModel.latestResultDataState.collectAsState()

        EmptyDataStateUi(exerciseDataState join latestResultDataState) { (exercise, result) ->
            ProvideDefaultExerciseTemplateStatus(exercise = exercise) {
                val resultTemplateStatus = LocalTemplateStatusProvider.current()

                val webViewState = getFeedbackViewWebViewState(
                    serverUrl = serverUrl,
                    courseId = exercise.course?.id ?: return@ProvideDefaultExerciseTemplateStatus,
                    exerciseId = exercise.id,
                    participationId = exercise.latestParticipation?.id ?: return@ProvideDefaultExerciseTemplateStatus,
                    resultId = result?.id ?: return@ProvideDefaultExerciseTemplateStatus,
                    templateStatus = resultTemplateStatus ?: return@ProvideDefaultExerciseTemplateStatus
                )

                var webView: WebView? by remember { mutableStateOf(null) }

                ArtemisWebView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    webViewState = webViewState,
                    webView = webView,
                    serverUrl = serverUrl,
                    authToken = authToken,
                    setWebView = { webView = it }
                )
            }
        }


        // Commented out because we use the web view for now.
//        val latestIndividualDueDate by viewModel.latestIndividualDueDate.collectAsState()
//
//        val feedbackItems by viewModel.feedbackItems.collectAsState()
//
//        val buildLogs by viewModel.buildLogs.collectAsState()
//
//        ExerciseDataStateUi(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            value = latestResultDataState join exerciseDataState,
//            onClickRetry = { viewModel.requestReloadExercise() },
//            onSuccess = { (latestResult, exercise) ->
//                ResultDetailUi(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(8.dp)
//                        .verticalScroll(rememberScrollState()),
//                    exercise = exercise,
//                    latestResult = latestResult ?: return@ExerciseDataStateUi,
//                    feedbackItems = feedbackItems.orElse(emptyList()),
//                    latestIndividualDueDate = latestIndividualDueDate.orElse(null),
//                    buildLogs = buildLogs.orElse(emptyList())
//                )
//            }
//        )
    }
}