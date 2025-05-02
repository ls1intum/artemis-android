package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat?,
    isLongToolbar: Boolean,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    actions: ExerciseActions
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(bottom =  32.dp)
    ) {
        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacings.ScreenTopBarSpacing)
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            exercise = exercise,
            actions = actions
        )

        Spacer(modifier = Modifier.height(16.dp))

       ExerciseChips(exercise)

        Spacer(modifier = Modifier.height(16.dp))

        if (exercise !is QuizExercise && webViewState != null) {
            ArtemisWebView(
                modifier = Modifier
                    .fillMaxWidth(),
                webViewState = webViewState,
                webView = webView,
                adjustHeightForContent = true,
                setWebView = setWebView
            )
        } else {
            Text(
                text = stringResource(id = R.string.exercise_view_overview_problem_statement_not_available),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // The Spacers in this Column are needed as verticalArrangement.spacedBy does lead to a gap under the ArtemisWebView
        Spacer(modifier = Modifier.height(16.dp))

        ExerciseInformation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .border(
                    width = informationTableThickness,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraSmall
                ),
            exercise = exercise,
            exerciseChannel = exerciseChannel,
            isLongToolbar = isLongToolbar
        )
    }
}