package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ProblemStatementWebView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier,
    exercise: Exercise,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    actions: ExerciseActions
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier)

        ParticipationStatusUi(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            actions = actions
        )

        Box(
            modifier = if (webViewState?.isLoading != true) Modifier.fillMaxWidth() else
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
        ) {
            if (webViewState != null) {
                ProblemStatementWebView(
                    modifier = Modifier.fillMaxWidth(),
                    webViewState = webViewState,
                    webView = webView,
                    setWebView = setWebView
                )
            }

            val loadingState = webViewState?.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.Center),
                    progress = loadingState.progress
                )
            }
        }
    }
}
