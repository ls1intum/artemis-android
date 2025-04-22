package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.courseId
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.getProblemStatementWebViewState
import kotlinx.coroutines.Deferred

val LocalExerciseScreenFloatingActionButton =
    compositionLocalOf { ExerciseScreenFloatingActionButtonProvider() }

/**
 * Display the exercise screen with tabs for the problem statement, the exercise info and the questions and answer.
 */
@Composable
internal fun ExerciseScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    onViewResult: () -> Unit,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long) -> Unit,
    onSidebarToggle: () -> Unit = {},
) {
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

    val exerciseDataState by viewModel.exerciseDataState.collectAsState()
    val channelDataState by viewModel.channelDataState.collectAsState()

    val courseId: Long? = exerciseDataState.courseId

    val exerciseId by remember(exerciseDataState) {
        derivedStateOf {
            exerciseDataState.bind { it.id }.orNull()
        }
    }

    val latestParticipationId by remember(exerciseDataState) {
        derivedStateOf {
            exerciseDataState.bind { exercise ->
                // Only relevant for programming exercises
                if (exercise is ProgrammingExercise) {
                    exercise.getSpecificStudentParticipation(false)?.id
                } else null
            }.orNull()
        }
    }

    val webViewState: WebViewState? = getProblemStatementWebViewState(
        serverUrl = artemisContext.serverUrl,
        courseId = courseId,
        exerciseId = exerciseId,
        participationId = latestParticipationId
    )

    // Retain web view instance to avoid reloading when switching between tabs
    var savedWebView: WebView? by remember(exerciseDataState) {
        mutableStateOf(null)
    }

    var startExerciseParticipationDeferred: Deferred<Long?>? by remember { mutableStateOf(null) }
    AwaitDeferredCompletion(startExerciseParticipationDeferred) { participationId ->
        if (participationId != null) {
            onViewTextExerciseParticipationScreen(participationId)
        }
    }

    Box(modifier = modifier) {
        val windowSizeClass = getWindowSizeClass()
        val isLongToolbar = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

        val currentExerciseScreenFloatingActionButton =
            remember { ExerciseScreenFloatingActionButtonProvider() }

        CompositionLocalProvider(LocalExerciseScreenFloatingActionButton provides currentExerciseScreenFloatingActionButton) {
            val floatingActionButton = @Composable {
                val currentSettings = currentExerciseScreenFloatingActionButton.settings.value
                if (currentSettings != null) {
                    FloatingActionButton(
                        onClick = currentSettings.onClick,
                    ) {
                        Icon(
                            imageVector = currentSettings.icon,
                            contentDescription = currentSettings.contentDescription
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    ExerciseScreenTopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        exerciseDataState = exerciseDataState
                    )
                },
                floatingActionButton = floatingActionButton
            ) { padding ->

                val onParticipateInQuizDelegate = { isPractice: Boolean ->
                    courseId?.let {
                        onParticipateInQuiz(it, isPractice)
                    }
                }

                val actions = remember(
                    courseId,
                    onViewTextExerciseParticipationScreen,
                    onParticipateInQuizDelegate,
                    onViewResult,
                    viewModel
                ) {
                    ExerciseActions(
                        onClickStartTextExercise = {
                            startExerciseParticipationDeferred = viewModel.startExercise()
                        },
                        onClickPracticeQuiz = { onParticipateInQuizDelegate(true) },
                        onClickOpenQuiz = { onParticipateInQuizDelegate(false) },
                        onClickStartQuiz = { onParticipateInQuizDelegate(false) },
                        onClickViewResult = onViewResult,
                        onClickOpenTextExercise = onViewTextExerciseParticipationScreen,
                        onClickViewQuizResults = {
                            courseId?.let {
                                onClickViewQuizResults(it)
                            }
                        }
                    )
                }

                ExerciseScreenBody(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = padding.calculateTopPadding())
                        .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                    exerciseDataState = exerciseDataState,
                    exerciseChannelDataState = channelDataState,
                    isLongToolbar = isLongToolbar,
                    actions = actions,
                    webViewState = webViewState,
                    setWebView = { savedWebView = it },
                    webView = savedWebView,
                    onClickRetry = viewModel::onRequestReload
                )
            }
        }
    }
}

data class ExerciseScreenFloatingActionButtonProvider(
    val settings: MutableState<FloatingActionButtonSettings?> = mutableStateOf(null)
)

data class FloatingActionButtonSettings(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit
)
