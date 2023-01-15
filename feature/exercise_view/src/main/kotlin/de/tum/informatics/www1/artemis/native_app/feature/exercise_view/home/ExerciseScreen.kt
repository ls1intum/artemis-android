package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

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
import androidx.navigation.NavController
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.canDisplayMetisOnDisplaySide
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
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
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit
) {
    val serverUrl: String by viewModel.serverUrl.collectAsState()
    val authToken: String by viewModel.authToken.collectAsState()

    val exerciseDataState by viewModel.exercise.collectAsState()

    val gradedParticipation by viewModel.gradedParticipation.collectAsState()

    val courseId: Long? by remember(exerciseDataState) {
        derivedStateOf {
            exerciseDataState.bind { it.course?.id }.orNull()
        }
    }

    val exerciseId by remember(exerciseDataState) {
        derivedStateOf {
            exerciseDataState.bind { it.id }.orNull()
        }
    }

    val metisContext by remember(courseId, exerciseId) {
        derivedStateOf {
            val currentCourseId = courseId
            val currentExerciseId = exerciseId
            if (currentCourseId != null && currentExerciseId != null) {
                MetisContext.Exercise(courseId = currentCourseId, exerciseId = currentExerciseId)
            } else null
        }
    }

    val url by remember(serverUrl, courseId, exerciseId) {
        derivedStateOf {
            if (courseId != null && exerciseId != null) {
                URLBuilder(serverUrl).apply {
                    appendPathSegments(
                        "courses",
                        courseId.toString(),
                        "exercises",
                        exerciseId.toString()
                    )
                }
                    .buildString()
            } else null
        }
    }

    val webViewState by remember(url) {
        derivedStateOf {
            val currentUrl = url
            if (currentUrl != null) {
                WebViewState(WebContent.Url(url = currentUrl))
            } else null
        }
    }

    // Retain web view instance to avoid reloading when switching between tabs
    var savedWebView: WebView? by remember {
        mutableStateOf(null)
    }

    BoxWithConstraints(modifier = modifier) {
        val windowSizeClass = getWindowSizeClass()
        // If true, the communication is not displayed in a tab but in a window on the right
        val displayCommunicationOnSide = canDisplayMetisOnDisplaySide(
            windowSizeClass = windowSizeClass,
            parentWidth = maxWidth,
            metisContentRatio = METIS_RATIO
        )

        // Only collapse toolbar if otherwise too much of the screen would be occupied by it
        val isToolbarCollapsible = windowSizeClass.heightSizeClass < WindowHeightSizeClass.Expanded

        val isLongToolbar = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

        // Keep state when device configuration changes
        val body = @Composable { modifier: Modifier ->
            ExerciseScreenBody(
                modifier = modifier,
                exerciseDataState = exerciseDataState,
                displayCommunicationOnSide = displayCommunicationOnSide,
                gradedParticipation = gradedParticipation,
                authToken = authToken,
                navController = navController,
                onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                onParticipateInQuiz = { isPractice ->
                    courseId?.let { onParticipateInQuiz(it, isPractice) }
                },
                onViewResult = onViewResult,
                onClickStartExercise = {
                    viewModel.startExercise(
                        onViewTextExerciseParticipationScreen
                    )
                },
                onClickRetry = viewModel::requestReloadExercise,
                metisContext = metisContext,
                webViewState = webViewState,
                setWebView = { savedWebView = it },
                webView = savedWebView
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
