package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SideBarMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipData
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.details.ExerciseDetailsTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview.ExerciseOverviewTab
import io.ktor.http.*
import me.onebone.toolbar.*
import org.koin.androidx.compose.get

private const val METIS_RATIO = 0.3f

/**
 * Display the exercise screen with tabs for the problem statement, the exercise info and the questions and answer.
 */
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
        val isToolbarCollapsible = windowSizeClass.heightSizeClass < WindowHeightSizeClass.Medium

        val state = rememberCollapsingToolbarScaffoldState()
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = state,
            toolbar = {
                ExerciseScreenCollapsingTopBar(
                    state = state,
                    exercise = exerciseDataState,
                    onNavigateBack = onNavigateBack
                )
            },
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            body = ExerciseScreenBody(
                viewModel,
                exerciseDataState,
                displayCommunicationOnSide,
                serverUrl,
                gradedParticipation,
                authToken,
                navController,
                onViewTextExerciseParticipationScreen,
                onParticipateInQuiz,
                onViewResult
            )
        )
    }

}

@Composable
private fun ExerciseScreenBody(
    viewModel: ExerciseViewModel,
    exerciseDataState: DataState<Exercise>,
    displayCommunicationOnSide: Boolean,
    serverUrl: String,
    gradedParticipation: Participation?,
    authToken: String,
    navController: NavController,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    onViewResult: () -> Unit
): @Composable() (CollapsingToolbarScaffoldScope.() -> Unit) =
    {
        Surface(Modifier.fillMaxSize()) {
            ExerciseDataStateUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                value = exerciseDataState
            ) { exercise ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val selectedTabIndexState = rememberSaveable { mutableStateOf(0) }
                    val selectedTabIndex by selectedTabIndexState

                    LaunchedEffect(displayCommunicationOnSide) {
                        if (displayCommunicationOnSide && selectedTabIndex == 2) {
                            selectedTabIndexState.value = 0
                        }
                    }

                    val courseId: Long = exercise.course?.id ?: return@Column

                    // Maybe add a replacement ui
                    val exerciseId = exercise.id

                    val metisContext = remember(courseId, exerciseId) {
                        MetisContext.Exercise(courseId = courseId, exerciseId)
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

                    // Retain web view instance to avoid reloading when switching between tabs
                    var savedWebView: WebView? by remember { mutableStateOf(null) }

                    val tabUi = @Composable { modifier: Modifier ->
                        ScreenWithTabsUi(
                            modifier = modifier,
                            selectedTabIndex = selectedTabIndex,
                            gradedParticipation = gradedParticipation,
                            displayCommunicationOnSide = displayCommunicationOnSide,
                            authToken = authToken,
                            exercise = exercise,
                            courseId = courseId,
                            webViewState = webViewState,
                            currentWebView = savedWebView,
                            metisContext = metisContext,
                            navController = navController,
                            onUpdateSelectedTabIndex = { selectedTabIndexState.value = it },
                            onClickStartExercise = {
                                viewModel.startExercise(
                                    onViewTextExerciseParticipationScreen
                                )
                            },
                            onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                            onParticipateInQuiz = onParticipateInQuiz,
                            setWebView = { savedWebView = it },
                            onViewResult = onViewResult
                        )
                    }

                    if (displayCommunicationOnSide) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            tabUi(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f - METIS_RATIO)
                            )

                            SideBarMetisUi(
                                modifier = Modifier
                                    .weight(METIS_RATIO)
                                    .fillMaxHeight(),
                                metisContext = metisContext,
                                navController = navController,
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.exercise_view_tab_qna),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            )
                        }
                    } else {
                        tabUi(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

@Composable
private fun CollapsingToolbarScope.ExerciseScreenCollapsingTopBar(
    state: CollapsingToolbarScaffoldState,
    exercise: DataState<Exercise>,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            TitleText(
                modifier = Modifier.graphicsLayer { alpha = 1f - state.toolbarState.progress },
                exerciseDataState = exercise
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )

    TopBarExerciseInformation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
            .parallax(0f),
        state = state,
        exercise = exercise
    )
}

@Composable
private fun TopBarExerciseInformation(
    modifier: Modifier,
    state: CollapsingToolbarScaffoldState,
    exercise: DataState<Exercise>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TitleText(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = state.toolbarState.progress },
            exerciseDataState = exercise,
            style = MaterialTheme.typography.headlineLarge
        )

        EmptyDataStateUi(
            dataState = exercise,
            otherwise = {
                ExerciseCategoryChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(true),
                    chips = listOf(
                        ExerciseCategoryChipData("WWWW", Color.Cyan),
                        ExerciseCategoryChipData("WWWW", Color.Cyan),
                        ExerciseCategoryChipData("WWWW", Color.Cyan)
                    )
                )
            }
        ) { loadedExercise ->
            ExerciseCategoryChipRow(modifier = Modifier.fillMaxWidth(), exercise = loadedExercise)
        }

        val currentUserPoints = exercise.bind { it.currentUserPoints }.orElse(null)
        val maxPoints = exercise.bind { it.maxPoints }.orElse(null)

        val text = when {
            currentUserPoints != null && maxPoints != null -> stringResource(
                id = R.string.exercise_view_overview_points_reached,
                currentUserPoints,
                maxPoints
            )
            maxPoints != null -> stringResource(
                id = R.string.exercise_view_overview_points_max,
                maxPoints
            )
            else -> stringResource(id = R.string.exercise_view_overview_points_none)
        }

        Text(
            modifier = Modifier.placeholder(exercise !is DataState.Success),
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExerciseScreenTopBar(
    displayCommunicationOnSide: Boolean,
    exerciseDataState: DataState<Exercise>,
    onNavigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val title = @Composable {
        TitleText(Modifier, exerciseDataState)
    }

    val navigationIcon = @Composable {
        IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
    }

    if (displayCommunicationOnSide) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            scrollBehavior = scrollBehavior
        )
    } else {
        MediumTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun TitleText(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    style: TextStyle = LocalTextStyle.current
) {
    val fontSize = style.fontSize

    val (titleText, inlineContent) = remember(exerciseDataState) {
        val text = buildAnnotatedString {
            appendInlineContent("icon")
            append(" ")
            append(
                exerciseDataState.bind { it.title }.orElse(null)
                    ?: "Exercise name placeholder"
            )
        }

        val inlineContent = mapOf(
            "icon" to InlineTextContent(
                Placeholder(
                    fontSize,
                    fontSize,
                    PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Icon(
                    imageVector = exerciseDataState.bind {
                        getExerciseTypeIcon(
                            it
                        )
                    }
                        .orElse(Icons.Default.Downloading),
                    contentDescription = null
                )
            }
        )

        text to inlineContent
    }

    Text(
        text = titleText,
        inlineContent = inlineContent,
        modifier = modifier.placeholder(exerciseDataState !is DataState.Success),
        style = style
    )
}

@Composable
private fun ScreenWithTabsUi(
    modifier: Modifier,
    selectedTabIndex: Int,
    gradedParticipation: Participation?,
    displayCommunicationOnSide: Boolean,
    authToken: String,
    exercise: Exercise,
    courseId: Long,
    webViewState: WebViewState,
    currentWebView: WebView?,
    metisContext: MetisContext.Exercise,
    navController: NavController,
    onUpdateSelectedTabIndex: (Int) -> Unit,
    onClickStartExercise: () -> Unit,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    setWebView: (WebView) -> Unit,
    onViewResult: () -> Unit
) {
    Column(modifier = modifier) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex
        ) {
            DefaultTab(
                0,
                Icons.Default.ViewHeadline,
                R.string.exercise_view_tab_overview,
                selectedTabIndex,
                onUpdateSelectedTabIndex
            )
            DefaultTab(
                1,
                Icons.Default.Info,
                R.string.exercise_view_tab_info,
                selectedTabIndex,
                onUpdateSelectedTabIndex
            )

            if (!displayCommunicationOnSide) {
                DefaultTab(
                    2,
                    Icons.Default.HelpCenter,
                    R.string.exercise_view_tab_qna,
                    selectedTabIndex,
                    onUpdateSelectedTabIndex
                )
            }
        }

        val scrollableTabModifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding()

        when (selectedTabIndex) {
            0 -> {
                ExerciseOverviewTab(
                    modifier = scrollableTabModifier,
                    authToken = authToken,
                    exercise = exercise,
                    gradedParticipation = gradedParticipation,
                    onClickStartExercise = onClickStartExercise,
                    onClickOpenTextExercise = onViewTextExerciseParticipationScreen,
                    onClickPracticeQuiz = {
                        onParticipateInQuiz(courseId, true)
                    },
                    onClickStartQuiz = {
                        onParticipateInQuiz(courseId, false)
                    },
                    onClickOpenQuiz = {
                        onParticipateInQuiz(courseId, false)
                    },
                    onViewResult = onViewResult,
                    webViewState = webViewState,
                    setWebView = setWebView,
                    webView = currentWebView
                )
            }

            1 -> {
                ExerciseDetailsTab(
                    modifier = scrollableTabModifier,
                    exercise = exercise,
                    latestResult = null
                )
            }

            2 -> {
                SmartphoneMetisUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    metisContext = metisContext,
                    navController = navController
                )
            }
        }
    }
}