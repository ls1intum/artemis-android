package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ComposeWebView
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

        ComposeWebView(
            modifier = Modifier.fillMaxWidth(),
            url = url,
            webViewClient = webViewClient
        )
    }
}

private class AuthWebClient(private val authToken: String) : WebViewClient() {
    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        setLocalStorage(view)
    }

    private fun setLocalStorage(view: WebView) {
        view.evaluateJavascript(
            """
                localStorage.setItem('jhi-authenticationtoken', JSON.stringify('$authToken'));
            """.trimIndent(), null
        )
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)

        setLocalStorage(view)
    }
}