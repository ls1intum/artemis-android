package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home

import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview.ExerciseOverviewTab
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat


@Composable
internal fun ExerciseScreenBody(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    exerciseChannelDataState: DataState<ChannelChat>,
    isLongToolbar: Boolean,
    actions: ExerciseActions,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    onClickRetry: () -> Unit
) {
    ExerciseDataStateUi(
        modifier = modifier,
        onClickRetry = onClickRetry,
        value = exerciseDataState,
        onSuccess = { exercise ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val exerciseChannel = exerciseChannelDataState.bind { it }.orNull()

                ExerciseOverviewTab(
                    modifier = Modifier.fillMaxSize(),
                    exercise = exercise,
                    exerciseChannel = exerciseChannel,
                    isLongToolbar = isLongToolbar,
                    webViewState = webViewState,
                    setWebView = setWebView,
                    webView = webView,
                    actions = actions,
                )

            }
        }
    )
}