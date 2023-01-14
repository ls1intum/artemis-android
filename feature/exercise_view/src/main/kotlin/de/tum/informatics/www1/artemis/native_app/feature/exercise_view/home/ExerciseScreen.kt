package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.annotation.StringRes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SideBarMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.details.ExerciseDetailsTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview.ExerciseOverviewTab
import io.ktor.http.*
import kotlinx.datetime.Instant
import me.onebone.toolbar.*
import org.koin.androidx.compose.get

private const val METIS_RATIO = 0.3f

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
                    onClickRetry = { viewModel.requestReloadExercise() }
                )
            }
        }

        if (isToolbarCollapsible) {
            val state = rememberCollapsingToolbarScaffoldState()

            LaunchedEffect(exerciseDataState) {
                if (exerciseDataState.isSuccess) {
                    state.toolbarState.expand()
                }
            }

            CollapsingToolbarScaffold(
                modifier = Modifier.fillMaxSize(),
                state = state,
                toolbar = {
                    ExerciseScreenCollapsingTopBar(
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
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = {},
                            navigationIcon = {
                                TopAppBarNavigationIcon(onNavigateBack = onNavigateBack)
                            },
                            actions = {
                                TopAppBarActions(onRequestRefresh = viewModel::requestReloadExercise)
                            }
                        )
                        TopBarExerciseInformation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp),
                            titleTextAlpha = 1f,
                            exercise = exerciseDataState,
                            isLongToolbar = isLongToolbar
                        )
                    }
                }
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

@Composable
private fun ExerciseScreenBody(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    displayCommunicationOnSide: Boolean,
    serverUrl: String,
    gradedParticipation: Participation?,
    authToken: String,
    navController: NavController,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    onViewResult: () -> Unit,
    onClickRetry: () -> Unit,
    onClickStartExercise: () -> Unit
) {
    ExerciseDataStateUi(
        modifier = modifier,
        onClickRetry = onClickRetry,
        value = exerciseDataState,
        onSuccess = { exercise ->
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
                        onClickStartExercise = onClickStartExercise,
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
    )
}

@Composable
private fun CollapsingToolbarScope.ExerciseScreenCollapsingTopBar(
    state: CollapsingToolbarScaffoldState,
    isLongToolbar: Boolean,
    exercise: DataState<Exercise>,
    onNavigateBack: () -> Unit,
    onRequestRefresh: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            TitleText(
                modifier = Modifier.graphicsLayer { alpha = 1f - state.toolbarState.progress },
                exerciseDataState = exercise,
                maxLines = 1
            )
        },
        navigationIcon = {
            TopAppBarNavigationIcon(onNavigateBack)
        },
        actions = {
            TopAppBarActions(onRequestRefresh = onRequestRefresh)
        }
    )

    TopBarExerciseInformation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
            .parallax(0f),
        titleTextAlpha = state.toolbarState.progress,
        exercise = exercise,
        isLongToolbar = isLongToolbar
    )
}

@Composable
private fun TopAppBarNavigationIcon(onNavigateBack: () -> Unit) {
    IconButton(onClick = onNavigateBack) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
    }
}

@Composable
private fun TopAppBarActions(onRequestRefresh: () -> Unit) {
    IconButton(onClick = onRequestRefresh) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
    }
}

private val placeholderCategoryChips = listOf(
    ExerciseCategoryChipData("WWWW", Color.Cyan),
    ExerciseCategoryChipData("WWWW", Color.Cyan),
    ExerciseCategoryChipData("WWWW", Color.Cyan)
)

/**
 * @param isLongToolbar if the deadline information is displayed on the right side of the toolbar.
 * If false, the information is instead displayed in the column
 */
@Composable
private fun TopBarExerciseInformation(
    modifier: Modifier,
    titleTextAlpha: Float,
    exercise: DataState<Exercise>,
    isLongToolbar: Boolean
) {
    val dueDate = exercise.bind { it.dueDate }.orElse(null)
    val assessmentDueData = exercise.bind { it.assessmentDueDate }.orElse(null)

    // Prepare ui that is movable between long and short toolbars

    val exerciseInfoUi = remember(exercise) {
        movableContentOf {
            EmptyDataStateUi(
                dataState = exercise,
                otherwise = {
                    ExerciseCategoryChipRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .placeholder(true),
                        chips = placeholderCategoryChips
                    )
                }
            ) { loadedExercise ->
                ExerciseCategoryChipRow(
                    modifier = Modifier.fillMaxWidth(),
                    exercise = loadedExercise
                )
            }

            val currentUserPoints = exercise.bind { it.currentUserPoints }.orElse(null)
            val maxPoints = exercise.bind { it.maxPoints }.orElse(null)

            val pointsHintText = when {
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
                text = pointsHintText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    val dueDateColumnUi = remember(exercise) {
        movableContentOf { modifier: Modifier ->
            var maxWidth: Int by remember { mutableStateOf(0) }
            val updateMaxWidth = { new: Int -> maxWidth = new }

            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dueDateTopBarTextInformation =
                    @Composable { date: Instant, hintRes: @receiver:StringRes Int ->
                        TopBarTextInformation(
                            modifier = Modifier.fillMaxWidth(),
                            hintColumnWidth = maxWidth,
                            hint = stringResource(id = hintRes),
                            dataText = getRelativeTime(to = date).toString(),
                            dataColor = getDueDateColor(date),
                            updateHintColumnWidth = updateMaxWidth
                        )
                    }

                dueDate?.let {
                    dueDateTopBarTextInformation(
                        it,
                        R.string.exercise_view_overview_hint_submission_due_date
                    )
                }

                assessmentDueData?.let {
                    dueDateTopBarTextInformation(
                        it,
                        R.string.exercise_view_overview_hint_assessment_due_date
                    )
                }
            }
        }
    }

    // Actual UI
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TitleText(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = titleTextAlpha },
            exerciseDataState = exercise,
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 2
        )

        // Here we make the distinction in the layout between long toolbar and short toolbar

        if (isLongToolbar) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    exerciseInfoUi()
                }

                dueDateColumnUi(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.Bottom)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                exerciseInfoUi()
                dueDateColumnUi(Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Text information composable that achieves a table like layout, where the hint is the first column
 * and the data is the second column.
 */
@Composable
private fun TopBarTextInformation(
    modifier: Modifier,
    hintColumnWidth: Int,
    hint: String,
    dataText: String,
    dataColor: Color?,
    updateHintColumnWidth: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val assignedWidth = maxOf(hintColumnWidth, placeable.width)
                if (assignedWidth > hintColumnWidth) {
                    updateHintColumnWidth(assignedWidth)
                }

                layout(width = assignedWidth, height = placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
        )

        val dataModifier = Modifier

        if (dataColor != null) {
            ExerciseInfoChip(modifier = dataModifier, color = dataColor, text = dataText)
        } else {
            Text(
                modifier = dataModifier.padding(horizontal = ExerciseInfoChipTextHorizontalPadding),
                text = dataText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun getDueDateColor(dueDate: Instant): Color =
    if (dueDate.hasPassed()) Color.Red else Color.Green

@Composable
private fun TitleText(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int
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
                    imageVector = exerciseDataState.bind { exercise ->
                        getExerciseTypeIcon(exercise)
                    }.orElse(Icons.Default.Downloading),
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
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
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