package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview

import android.graphics.Bitmap
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier,
    serverUrl: String,
    authToken: String,
    courseId: Long,
    exercise: Exercise,
    gradedParticipation: Participation?,
    onClickStartExercise: () -> Unit,
    onClickOpenTextExercise: (participationId: Long) -> Unit,
    onClickPracticeQuiz: () -> Unit,
    onClickStartQuiz: () -> Unit,
    onClickOpenQuiz: () -> Unit,
    onViewResult: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spacer(modifier = Modifier)

        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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

        val url = remember(serverUrl, courseId, exercise.id) {
            URLBuilder(serverUrl).apply {
                appendPathSegments(
                    "courses",
                    courseId.toString(),
                    "exercises",
                    exercise.id.toString()
                )
            }
                .buildString()
        }

        val webViewState = rememberWebViewState(url = url)

        Box(
            modifier = if (!webViewState.isLoading) Modifier.fillMaxWidth() else
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
        ) {
            WebView(
                modifier = Modifier.fillMaxWidth(),
                state = webViewState,
                client = webViewClient,
                onCreated = {
                    it.settings.javaScriptEnabled = true
                    it.settings.domStorageEnabled = true
                    it.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                }
            )

            when (val loadingState = webViewState.loadingState) {
                LoadingState.Initializing -> {

                }

                LoadingState.Finished -> {
                }

                is LoadingState.Loading -> {
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