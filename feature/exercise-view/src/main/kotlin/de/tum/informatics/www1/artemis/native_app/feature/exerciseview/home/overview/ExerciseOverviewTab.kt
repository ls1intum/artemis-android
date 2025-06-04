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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ProvideDefaultExerciseTemplateStatus
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
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ProvideDefaultExerciseTemplateStatus(exercise = exercise) {
                ExerciseActionButtons(
                    modifier = Modifier,
                    exercise = exercise,
                    actions = actions
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 72.dp)
        ) {
            ParticipationStatusUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacings.ScreenTopBarSpacing)
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                exercise = exercise
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExerciseOverviewChips(exercise = exercise)

            Spacer(modifier = Modifier.height(16.dp))

            if (exercise is ProgrammingExercise) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    text = stringResource(id = R.string.exercise_view_overview_problem_statement),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (exercise !is QuizExercise && webViewState != null) {
                Spacer(modifier = Modifier.height(8.dp))

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
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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
}
