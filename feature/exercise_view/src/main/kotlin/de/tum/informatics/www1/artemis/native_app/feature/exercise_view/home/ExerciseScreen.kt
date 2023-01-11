package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.details.ExerciseDetailsTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview.ExerciseOverviewTab
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
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

    val topAppBarState = rememberTopAppBarState()


    val gradedParticipation by viewModel.gradedParticipation.collectAsState(initial = null)

    BoxWithConstraints(modifier = modifier) {
        // If true, the communication is not displayed in a tab but in a window on the right
        val displayCommunicationOnSide =
            windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
                    && (maxWidth * METIS_RATIO) >= 300.dp

        val scrollBehavior = if (displayCommunicationOnSide) {
            TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
        } else TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ExerciseScreenTopBar(
                    displayCommunicationOnSide = displayCommunicationOnSide,
                    exerciseDataState = exerciseDataState,
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            var triggeredRefreshManually by remember { mutableStateOf(false) }
            val isRefreshing = triggeredRefreshManually && exerciseDataState is DataState.Loading

            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

            val triggerRefresh = {
                triggeredRefreshManually = true
                viewModel.requestReloadExercise()
            }

            ExerciseDataStateUi(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                            displayCommunicationOnSide = displayCommunicationOnSide,
                            swipeRefreshState = swipeRefreshState,
                            triggerRefresh = triggerRefresh,
                            authToken = authToken,
                            exercise = exercise,
                            onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                            onParticipateInQuiz = onParticipateInQuiz,
                            courseId = courseId,
                            onViewResult = onViewResult,
                            webViewState = webViewState,
                            currentWebView = savedWebView,
                            metisContext = metisContext,
                            navController = navController,
                            gradedParticipation = gradedParticipation,
                            onUpdateSelectedTabIndex = { selectedTabIndexState.value = it },
                            setWebView = { savedWebView = it },
                            onClickStartExercise = {
                                viewModel.startExercise(
                                    onViewTextExerciseParticipationScreen
                                )
                            }
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
                        tabUi(
                            Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
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
        val fontSize = LocalTextStyle.current.fontSize

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
            modifier = Modifier.placeholder(exerciseDataState !is DataState.Success)
        )
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
private fun ScreenWithTabsUi(
    modifier: Modifier,
    selectedTabIndex: Int,
    gradedParticipation: Participation?,
    displayCommunicationOnSide: Boolean,
    swipeRefreshState: SwipeRefreshState,
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
    onViewResult: () -> Unit,
    triggerRefresh: () -> Unit
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
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = triggerRefresh
                ) {
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
            }

            1 -> {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = triggerRefresh
                ) {
                    ExerciseDetailsTab(
                        modifier = scrollableTabModifier,
                        exercise = exercise,
                        latestResult = null
                    )
                }
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