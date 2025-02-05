package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.LocalTemplateStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.getFeedbackViewWebViewState

@Composable
internal fun ViewResultScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    onCloseResult: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                shadowElevation = Spacings.AppBarElevation
            ){
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.result_view_title)) },
                    navigationIcon = {
                        IconButton(onClick = onCloseResult) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
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
                    exerciseId = exercise.id ?: 0L,
                    participationId = exercise.getSpecificStudentParticipation(false)?.id ?: return@ProvideDefaultExerciseTemplateStatus,
                    resultId = result?.id ?: return@ProvideDefaultExerciseTemplateStatus,
                    templateStatus = resultTemplateStatus ?: return@ProvideDefaultExerciseTemplateStatus
                )

                var webView: WebView? by remember { mutableStateOf(null) }

                Box(
                    modifier = Modifier
                        .padding(top = padding.calculateTopPadding())
                        .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                ){
                    ArtemisWebView(
                        modifier = Modifier.align(Alignment.Center),
                        webViewState = webViewState,
                        webView = webView,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        setWebView = { webView = it }
                    )
                }
            }
        }
    }
}
