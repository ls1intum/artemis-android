package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import io.ktor.http.*
import me.onebone.toolbar.*
import org.koin.androidx.compose.get

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
    windowSizeClass: WindowSizeClass,
    onNavigateBack: () -> Unit,
    onViewResult: () -> Unit,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit
) {
    val serverConfigurationService: ServerConfigurationService = get()
    val serverUrl: String by serverConfigurationService.serverUrl.collectAsState(initial = "")

    val accountService: AccountService = get()
    val authToken: String by accountService.authToken.collectAsState(initial = "")

    val exerciseDataState by viewModel.exercise.collectAsState(initial = DataState.Loading())

    val gradedParticipation by viewModel.gradedParticipation.collectAsState(initial = null)

    BoxWithConstraints(modifier = modifier) {
        // If true, the communication is not displayed in a tab but in a window on the right
        val displayCommunicationOnSide =
            windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
                    && (maxWidth * METIS_RATIO) >= 300.dp

        // Only collapse toolbar if otherwise too much of the screen would be occupied by it
        val isToolbarCollapsible = windowSizeClass.heightSizeClass < WindowHeightSizeClass.Expanded

        val isLongToolbar = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

        // Keep state when device configuration changes
        val body = remember {
            movableContentOf { modifier: Modifier ->
                ExerciseScreenBody(
                    modifier = modifier,
                    exerciseDataState = exerciseDataState,
                    displayCommunicationOnSide = displayCommunicationOnSide,
                    serverUrl = serverUrl,
                    gradedParticipation = gradedParticipation,
                    authToken = authToken,
                    navController = navController,
                    onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                    onParticipateInQuiz = onParticipateInQuiz,
                    onViewResult = onViewResult,
                    onClickStartExercise = {
                        viewModel.startExercise(
                            onViewTextExerciseParticipationScreen
                        )
                    },
                    onClickRetry = viewModel::requestReloadExercise
                )
            }
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

                LaunchedEffect(exerciseDataState) {
                    if (exerciseDataState.isSuccess) {
                        state.toolbarState.expand()
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
