package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ArtemisWebView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    webViewState: WebViewState?,
    serverUrl: String,
    authToken: String,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    actions: ExerciseActions
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise,
            actions = actions
        )

        if (exercise !is QuizExercise && webViewState != null) {
            ArtemisWebView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                webViewState = webViewState,
                webView = webView,
                serverUrl = serverUrl,
                authToken = authToken,
                setWebView = setWebView
            )
        } else {
            Text(
                text = stringResource(id = R.string.exercise_view_overview_problem_statement_not_available),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Gray
            )
        }
    }
}
