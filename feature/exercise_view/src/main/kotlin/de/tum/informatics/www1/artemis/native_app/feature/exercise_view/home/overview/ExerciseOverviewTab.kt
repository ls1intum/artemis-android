package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier,
    authToken: String,
    exercise: Exercise,
    gradedParticipation: Participation?,
    onClickStartExercise: () -> Unit,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit,
    onViewResult: () -> Unit,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier)

        ParticipationStatusUi(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            gradedParticipation = gradedParticipation,
            onClickViewResult = onViewResult,
            onClickStartExercise = onClickStartExercise,
            onClickOpenTextExercise = onClickOpenTextExercise,
            onClickOpenQuiz = onClickOpenQuiz,
            onClickPracticeQuiz = onClickPracticeQuiz,
            onClickStartQuiz = onClickStartQuiz
        )

        val webViewClient = remember(authToken) {
            AuthWebClient(authToken)
        }

        Box(
            modifier = if (webViewState?.isLoading != true) Modifier.fillMaxWidth() else
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
        ) {
            if (webViewState != null) {
                WebView(
                    modifier = Modifier.fillMaxWidth(),
                    state = webViewState,
                    client = webViewClient,
                    onCreated = {
                        it.settings.javaScriptEnabled = true
                        it.settings.domStorageEnabled = true
                        it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    },
                    factory = { context ->
                        if (webView != null) {
                            webView
                        } else {
                            val newWebView = WebView(context)
                            setWebView(newWebView)
                            newWebView
                        }
                    }
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

private class AuthWebClient(
    private val authToken: String
) : AccompanistWebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (view != null) {
            setLocalStorage(view)
        }

        super.onPageStarted(view, url, favicon)
    }


    private fun setLocalStorage(view: WebView) {
        view.evaluateJavascript(
            """
                localStorage.setItem('jhi-authenticationtoken', JSON.stringify('$authToken'));
            """.trimIndent(), null
        )
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (view != null) {
            setLocalStorage(view)
        }

        super.onPageFinished(view, url)
    }
}