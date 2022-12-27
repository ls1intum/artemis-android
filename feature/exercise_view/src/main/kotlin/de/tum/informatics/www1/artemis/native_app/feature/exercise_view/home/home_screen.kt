package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.details.ExerciseDetailsTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.tabs.overview.ExerciseOverviewTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R

/**
 * Display the exercise screen with tabs for the problem statement, the exercise info and the questions and answer.
 */
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
    val exerciseDataState = viewModel.exercise.collectAsState(initial = DataState.Loading()).value

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            MediumTopAppBar(
                title = {
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
                                Placeholder(fontSize, fontSize, PlaceholderVerticalAlign.TextCenter)
                            ) {
                                Icon(
                                    imageVector = exerciseDataState.bind { getExerciseTypeIcon(it) }
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
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
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
                var selectedTabIndex by selectedTabIndexState

                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedTabIndex
                ) {
                    DefaultTab(0, Icons.Default.ViewHeadline, R.string.exercise_view_tab_overview, selectedTabIndexState)
                    DefaultTab(1, Icons.Default.Info, R.string.exercise_view_tab_info, selectedTabIndexState)
                    DefaultTab(2, Icons.Default.HelpCenter, R.string.exercise_view_tab_qna, selectedTabIndexState)
                }

                val tabModifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )

                when (selectedTabIndex) {
                    0 -> {
                        val gradedParticipation = viewModel.gradedParticipation.collectAsState(
                            initial = null
                        ).value

                        val courseId: Long = exercise.course?.id ?: return@Column

                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = triggerRefresh
                        ) {
                            ExerciseOverviewTab(
                                modifier = tabModifier,
                                exercise = exercise,
                                gradedParticipation = gradedParticipation,
                                onViewResult = onViewResult,
                                onClickStartExercise = {
                                    viewModel.startExercise(onViewTextExerciseParticipationScreen)
                                },
                                onClickOpenTextExercise = onViewTextExerciseParticipationScreen,
                                onClickPracticeQuiz = {
                                    onParticipateInQuiz(courseId, true)
                                },
                                onClickStartQuiz = {
                                    onParticipateInQuiz(courseId, false)
                                },
                                onClickOpenQuiz = {
                                    onParticipateInQuiz(courseId, false)
                                }
                            )
                        }
                    }

                    1 -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = triggerRefresh
                        ) {
                            ExerciseDetailsTab(
                                modifier = tabModifier,
                                exercise = exercise,
                                latestResult = null
                            )
                        }
                    }

                    2 -> {
                        // Maybe add a replacement ui
                        val courseId = exercise.course?.id ?: return@Column
                        val exerciseId = exercise.id ?: return@Column

                        val metisContext = remember(courseId, exerciseId) {
                            MetisContext.Exercise(courseId = courseId, exerciseId)
                        }

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


    }
}