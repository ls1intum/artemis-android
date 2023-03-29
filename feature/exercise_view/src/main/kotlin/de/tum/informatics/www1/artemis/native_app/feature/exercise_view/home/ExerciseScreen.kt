package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.courseId
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.getProblemStatementWebViewState
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.canDisplayMetisOnDisplaySide
import io.ktor.http.*
import me.onebone.toolbar.*

val LocalExerciseScreenFloatingActionButton =
    compositionLocalOf { ExerciseScreenFloatingActionButtonProvider() }

/**
 * Display the exercise screen with tabs for the problem statement, the exercise info and the questions and answer.
 */
@OptIn(ExperimentalToolbarApi::class)
@Composable
internal fun ExerciseScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onViewResult: () -> Unit,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long) -> Unit
) {
    val serverUrl: String by viewModel.serverUrl.collectAsState()
    val authToken: String by viewModel.authToken.collectAsState()

    val exerciseDataState by viewModel.exerciseDataState.collectAsState()

    val courseId: Long? = exerciseDataState.courseId

    val exerciseId by remember(exerciseDataState) {
        derivedStateOf {
            exerciseDataState.bind { it.id }.orNull()
        }
    }

    val metisContext by remember(courseId, exerciseId) {
        derivedStateOf {
            val currentExerciseId = exerciseId
            if (courseId != null && currentExerciseId != null) {
                MetisContext.Exercise(courseId = courseId, exerciseId = currentExerciseId)
            } else null
        }
    }

    val webViewState: WebViewState? = getProblemStatementWebViewState(
        serverUrl = serverUrl,
        courseId = courseId,
        exerciseId = exerciseId
    )

    LaunchedEffect(serverUrl, authToken) {
        CookieManager.getInstance().setCookie(serverUrl, "jwt=$authToken")
    }

    // Retain web view instance to avoid reloading when switching between tabs
    var savedWebView: WebView? by remember(exerciseDataState) {
        mutableStateOf(null)
    }

    Box(modifier = modifier) {
        val windowSizeClass = getWindowSizeClass()
        // If true, the communication is not displayed in a tab but in a window on the right
        val displayCommunicationOnSide = canDisplayMetisOnDisplaySide(
            windowSizeClass = windowSizeClass,
            parentWidth = LocalConfiguration.current.screenWidthDp.dp,
            metisContentRatio = METIS_RATIO
        )

        // Only collapse toolbar if otherwise too much of the screen would be occupied by it
        val isToolbarCollapsible = windowSizeClass.heightSizeClass < WindowHeightSizeClass.Expanded

        val isLongToolbar = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

        // Keep state when device configuration changes
        val body = @Composable { modifier: Modifier ->
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
                        viewModel.startExercise(onViewTextExerciseParticipationScreen)
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
                modifier = modifier,
                exerciseDataState = exerciseDataState,
                displayCommunicationOnSide = displayCommunicationOnSide,
                navController = navController,
                metisContext = metisContext,
                actions = actions,
                webViewState = webViewState,
                setWebView = { savedWebView = it },
                webView = savedWebView,
                onClickRetry = viewModel::requestReloadExercise
            )
        }

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

            if (isToolbarCollapsible) {
                val state = rememberCollapsingToolbarScaffoldState()
                // On the first load, we need to expand the toolbar, as otherwise content may be hidden
                var hasExecutedInitialExpand by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(exerciseDataState, hasExecutedInitialExpand) {
                    if (exerciseDataState.isSuccess && !hasExecutedInitialExpand) {
                        state.toolbarState.expand()
                        hasExecutedInitialExpand = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = floatingActionButton
                ) { padding ->
                    CollapsingToolbarScaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        state = state,
                        toolbar = {
                            ExerciseScreenCollapsingTopBar(
                                modifier = Modifier.fillMaxWidth(),
                                state = state,
                                exercise = exerciseDataState,
                                onNavigateBack = onNavigateBack,
                                onRequestRefresh = viewModel::requestReloadExercise,
                                isLongToolbar = isLongToolbar
                            )
                        },
                        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                        body = {
                            Surface(Modifier.fillMaxSize()) {
                                body(Modifier.fillMaxSize())
                            }
                        }
                    )
                }
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        StaticTopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            onNavigateBack = onNavigateBack,
                            exerciseDataState = exerciseDataState,
                            isLongToolbar = isLongToolbar,
                            onRequestReloadExercise = viewModel::requestReloadExercise
                        )
                    },
                    floatingActionButton = floatingActionButton
                ) { padding ->
                    body(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
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
